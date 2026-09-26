package org.example.templatejava6.order.service;

import jakarta.mail.internet.MimeMessage;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.order.model.request.TaoYeuCauMuaSoLuongLonRequest;
import org.example.templatejava6.product.entity.ChiTietSanPham;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yêu cầu mua số lượng lớn: validate + gửi email cửa hàng.
 * Không lưu database, không tạo thông báo admin.
 */
@Service
public class YeuCauMuaSoLuongLonService {

    private static final Logger log = LoggerFactory.getLogger(YeuCauMuaSoLuongLonService.class);
    private static final int MIN_SO_LUONG = 16;
    private static final long SPAM_WINDOW_MS = 5L * 60L * 1000L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final JavaMailSender mailSender;

    /** key = sdt|idCtsp → epoch millis lần gửi gần nhất */
    private final ConcurrentHashMap<String, Long> recentSends = new ConcurrentHashMap<>();

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:SUNOVA}")
    private String fromName;

    @Value("${app.bulk-order.email-nhan:vu010746@gmail.com}")
    private String emailNhan;

    public YeuCauMuaSoLuongLonService(
            ChiTietSanPhamRepository chiTietSanPhamRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public Map<String, Object> guiYeuCau(TaoYeuCauMuaSoLuongLonRequest req) {
        if (req.getSoLuong() == null || req.getSoLuong() < MIN_SO_LUONG) {
            throw new ApiException(
                    "Tối thiểu " + MIN_SO_LUONG + " sản phẩm cho đơn số lượng lớn",
                    "INVALID_QTY");
        }

        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdWithSanPham(req.getIdChiTietSanPham())
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm", "NOT_FOUND"));

        int ton = ct.getSoLuongTon() != null ? ct.getSoLuongTon() : 0;
        if (req.getSoLuong() > ton) {
            throw new ApiException(
                    "Số lượng yêu cầu vượt quá tồn kho hiện có (còn " + ton + " sản phẩm)",
                    "INVALID_QTY");
        }

        String hoTen = normalizeSpaces(req.getHoTen());
        if (hoTen == null || hoTen.length() < 2 || hoTen.length() > 60) {
            throw new ApiException("Họ tên từ 2–60 ký tự", "INVALID_NAME");
        }

        String sdt = req.getSoDienThoai() != null
                ? req.getSoDienThoai().replaceAll("\\D", "")
                : "";
        if (!sdt.matches("^0\\d{9}$")) {
            throw new ApiException(
                    "Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)",
                    "INVALID_PHONE");
        }

        String email = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ApiException("Email không hợp lệ", "INVALID_EMAIL");
        }

        String tenCongTy = blankToNull(req.getTenCongTy());
        if (tenCongTy != null && tenCongTy.length() > 100) {
            throw new ApiException("Tên công ty tối đa 100 ký tự", "INVALID_COMPANY");
        }

        String ghiChu = blankToNull(req.getGhiChu());
        if (ghiChu != null && ghiChu.length() > 500) {
            throw new ApiException("Ghi chú tối đa 500 ký tự", "INVALID_NOTE");
        }

        purgeExpiredSpamEntries();
        String spamKey = sdt + "|" + ct.getId();
        Long last = recentSends.get(spamKey);
        long now = Instant.now().toEpochMilli();
        if (last != null && now - last < SPAM_WINDOW_MS) {
            throw new ApiException(
                    "Yêu cầu đã được gửi, vui lòng chờ SUNOVA liên hệ",
                    "DUPLICATE_REQUEST");
        }

        boolean loggedIn = isKhachDangNhap();
        boolean nhanKm = Boolean.TRUE.equals(req.getNhanKhuyenMai());
        String tenSp = tenSanPham(ct);
        String bienThe = bienThe(ct);

        try {
            guiEmail(ct, req.getSoLuong(), ton, hoTen, sdt, email, tenCongTy, ghiChu, nhanKm, loggedIn, tenSp, bienThe);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[BULK-ORDER] Gửi email thất bại: {}", ex.getMessage());
            throw new ApiException(
                    "Chưa gửi được yêu cầu, vui lòng thử lại hoặc gọi hotline 1900 6868",
                    "MAIL_FAILED");
        }

        recentSends.put(spamKey, now);
        return Map.of(
                "status", "OK",
                "message", "SUNOVA đã nhận yêu cầu. Chúng tôi sẽ liên hệ sớm.");
    }

    private void guiEmail(
            ChiTietSanPham ct,
            int soLuong,
            int ton,
            String hoTen,
            String sdt,
            String emailKhach,
            String tenCongTy,
            String ghiChu,
            boolean nhanKm,
            boolean loggedIn,
            String tenSp,
            String bienThe) throws Exception {
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            throw new ApiException(
                    "Chưa gửi được yêu cầu, vui lòng thử lại hoặc gọi hotline 1900 6868",
                    "MAIL_FAILED");
        }
        String to = emailNhan != null ? emailNhan.trim() : "";
        if (to.isBlank()) {
            throw new ApiException(
                    "Chưa gửi được yêu cầu, vui lòng thử lại hoặc gọi hotline 1900 6868",
                    "MAIL_FAILED");
        }

        String subject = "[SUNOVA] Yêu cầu mua số lượng lớn – " + tenSp + " x " + soLuong;
        String html = buildHtml(
                LocalDateTime.now().format(DATE_FMT),
                tenSp,
                bienThe,
                ct.getSku(),
                soLuong,
                ton,
                hoTen,
                sdt,
                emailKhach,
                tenCongTy,
                ghiChu,
                nhanKm,
                loggedIn);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(String.format("%s <%s>", fromName, fromEmail));
        helper.setTo(to);
        helper.setReplyTo(emailKhach);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
        log.info("[BULK-ORDER] Đã gửi yêu cầu {} x{} tới hộp thư cửa hàng", tenSp, soLuong);
    }

    private static String buildHtml(
            String thoiGian,
            String tenSp,
            String bienThe,
            String sku,
            int soLuong,
            int ton,
            String hoTen,
            String sdt,
            String email,
            String tenCongTy,
            String ghiChu,
            boolean nhanKm,
            boolean loggedIn) {
        StringBuilder company = new StringBuilder();
        if (tenCongTy != null && !tenCongTy.isBlank()) {
            company.append(row("Tên công ty", esc(tenCongTy)));
        }
        StringBuilder note = new StringBuilder();
        if (ghiChu != null && !ghiChu.isBlank()) {
            note.append(row("Ghi chú", esc(ghiChu)));
        }
        String bienTheRow = (bienThe != null && !bienThe.isBlank())
                ? row("Biến thể", esc(bienThe))
                : "";
        String skuRow = (sku != null && !sku.isBlank()) ? row("SKU", esc(sku)) : "";

        return """
                <div style="font-family:Segoe UI,Arial,sans-serif;background:#f7f3ee;padding:24px;">
                  <div style="max-width:640px;margin:0 auto;background:#fffaf5;border:1px solid #e8ddd0;border-radius:12px;overflow:hidden;">
                    <div style="background:#3d2c22;color:#f5ebe0;padding:18px 22px;">
                      <div style="font-size:18px;font-weight:700;letter-spacing:0.02em;">SUNOVA</div>
                      <div style="font-size:13px;opacity:0.85;margin-top:4px;">Yêu cầu mua số lượng lớn</div>
                    </div>
                    <div style="padding:22px;">
                      <p style="margin:0 0 16px;color:#6b5a4e;font-size:13px;">Thời gian gửi: <strong style="color:#3d2c22;">%s</strong></p>
                      <h3 style="margin:0 0 10px;color:#3d2c22;font-size:15px;">Sản phẩm</h3>
                      <table style="width:100%%;border-collapse:collapse;margin-bottom:18px;font-size:14px;color:#3d2c22;">
                        %s
                        %s
                        %s
                        %s
                        %s
                      </table>
                      <h3 style="margin:0 0 10px;color:#3d2c22;font-size:15px;">Khách hàng</h3>
                      <table style="width:100%%;border-collapse:collapse;font-size:14px;color:#3d2c22;">
                        %s
                        %s
                        %s
                        %s
                        %s
                        %s
                        %s
                      </table>
                      <p style="margin:18px 0 0;color:#8a7566;font-size:12px;">Bấm Trả lời để gửi email trực tiếp cho khách.</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                esc(thoiGian),
                row("Tên sản phẩm", esc(tenSp)),
                bienTheRow,
                skuRow,
                row("Số lượng yêu cầu", String.valueOf(soLuong)),
                row("Tồn kho hiện tại", String.valueOf(ton)),
                row("Họ tên", esc(hoTen)),
                row("Số điện thoại", esc(sdt)),
                row("Email", esc(email)),
                company,
                note,
                row("Nhận tin khuyến mãi", nhanKm ? "Có" : "Không"),
                row("Loại khách", loggedIn ? "Đã đăng nhập" : "Khách vãng lai"));
    }

    private static String row(String label, String value) {
        return """
                <tr>
                  <td style="padding:8px 0;border-bottom:1px solid #efe6dc;width:42%%;color:#8a7566;">%s</td>
                  <td style="padding:8px 0;border-bottom:1px solid #efe6dc;font-weight:600;">%s</td>
                </tr>
                """.formatted(label, value);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void purgeExpiredSpamEntries() {
        long cutoff = Instant.now().toEpochMilli() - SPAM_WINDOW_MS;
        Iterator<Map.Entry<String, Long>> it = recentSends.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (e.getValue() == null || e.getValue() < cutoff) {
                it.remove();
            }
        }
    }

    private static String tenSanPham(ChiTietSanPham ct) {
        SanPham sp = ct.getSanPham();
        if (sp != null && sp.getTen() != null && !sp.getTen().isBlank()) {
            return sp.getTen();
        }
        return ct.getSku() != null ? ct.getSku() : ("#" + ct.getId());
    }

    private static String bienThe(ChiTietSanPham ct) {
        StringBuilder sb = new StringBuilder();
        if (ct.getMauSac() != null && ct.getMauSac().getTen() != null) {
            sb.append(ct.getMauSac().getTen());
        }
        if (ct.getDungTichMl() != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(ct.getDungTichMl().stripTrailingZeros().toPlainString()).append(" ml");
        }
        return sb.toString();
    }

    private static boolean isKhachDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_KHACH_HANG"::equals);
    }

    private static String blankToNull(String s) {
        return normalizeSpaces(s);
    }

    private static String normalizeSpaces(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }
}
