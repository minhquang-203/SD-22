package org.example.templatejava6.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.example.templatejava6.order.entity.HoaDon;
import org.example.templatejava6.order.model.response.HoaDonChiTietResponse;
import org.example.templatejava6.order.repository.HoaDonChiTietRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gửi email "hóa đơn điện tử" cho khách khi đặt hàng online thành công.
 * Mọi lỗi gửi mail đều được nuốt (log) để không ảnh hưởng luồng đặt hàng / thanh toán.
 */
@Service
public class OrderMailService {

    private static final Logger log = LoggerFactory.getLogger(OrderMailService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:SUNOVA}")
    private String fromName;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public OrderMailService(HoaDonChiTietRepository hoaDonChiTietRepository,
                            org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void guiHoaDonDatHangThanhCong(HoaDon hoaDon) {
        try {
            if (hoaDon == null) {
                return;
            }
            String toEmail = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getEmail() : null;
            if (toEmail == null || toEmail.isBlank()) {
                log.warn("[MAIL] Hóa đơn {} không có email khách, bỏ qua gửi hóa đơn điện tử.", hoaDon.getMaHoaDon());
                return;
            }

            List<HoaDonChiTietResponse> lines = hoaDonChiTietRepository.findByIdHoaDon(hoaDon).stream()
                    .map(HoaDonChiTietResponse::new)
                    .toList();
            String html = buildInvoiceHtml(hoaDon, lines);
            String subject = "Xác nhận đơn hàng " + hoaDon.getMaHoaDon() + " — SUNOVA";

            if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
                log.warn("[MAIL-DEV] Mail chưa cấu hình. Bỏ qua gửi hóa đơn điện tử đơn {} tới {}.",
                        hoaDon.getMaHoaDon(), toEmail);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(String.format("%s <%s>", fromName, fromEmail));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[MAIL] Đã gửi hóa đơn điện tử đơn {} tới {}", hoaDon.getMaHoaDon(), toEmail);
        } catch (Exception ex) {
            log.error("[MAIL] Lỗi gửi hóa đơn điện tử: {}", ex.getMessage(), ex);
        }
    }

    /** Bao khach yeu cau tra hang da duoc duyet, huong dan tao van don tra ve shop. */
    public void guiYeuCauTraHangDuocDuyet(HoaDon hoaDon) {
        String noiDung = """
                <p style="margin:0 0 14px;color:#4b5563;line-height:1.6;">
                  Yêu cầu trả hàng cho đơn <strong>%s</strong> đã được <strong>duyệt</strong>.
                  Vui lòng tạo vận đơn hoàn trả để gửi sản phẩm về cửa hàng. Sau khi cửa hàng nhận được
                  hàng, chúng tôi sẽ tiến hành hoàn tiền cho bạn.
                </p>
                """.formatted(esc(safe(hoaDon != null ? hoaDon.getMaHoaDon() : "")));
        guiEmailThongBao(hoaDon, "Yêu cầu trả hàng đã được duyệt", "Yêu cầu trả hàng được duyệt", noiDung);
    }

    /** Bao khach yeu cau tra hang bi tu choi kem ly do. */
    public void guiYeuCauTraHangBiTuChoi(HoaDon hoaDon, String lyDo) {
        String noiDung = """
                <p style="margin:0 0 14px;color:#4b5563;line-height:1.6;">
                  Rất tiếc, yêu cầu trả hàng cho đơn <strong>%s</strong> đã bị <strong>từ chối</strong>.
                </p>
                <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:10px;padding:12px 16px;color:#9a3412;">
                  Lý do: %s
                </div>
                """.formatted(
                esc(safe(hoaDon != null ? hoaDon.getMaHoaDon() : "")),
                esc(lyDo != null && !lyDo.isBlank() ? lyDo : "Không có"));
        guiEmailThongBao(hoaDon, "Yêu cầu trả hàng bị từ chối", "Yêu cầu trả hàng bị từ chối", noiDung);
    }

    /** Bao khach da hoan tien thanh cong. */
    public void guiHoanTienHoanTat(HoaDon hoaDon, BigDecimal soTien, String maGiaoDich) {
        String maGd = maGiaoDich != null && !maGiaoDich.isBlank()
                ? "<div style=\"color:#8a8f98;font-size:13px;margin-top:6px;\">Mã giao dịch hoàn tiền: "
                        + esc(maGiaoDich) + "</div>"
                : "";
        String noiDung = """
                <p style="margin:0 0 14px;color:#4b5563;line-height:1.6;">
                  Chúng tôi đã hoàn tiền cho đơn <strong>%s</strong>.
                </p>
                <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:14px 16px;">
                  <div style="color:#166534;font-weight:800;font-size:18px;">Số tiền hoàn: %s</div>
                  %s
                </div>
                """.formatted(
                esc(safe(hoaDon != null ? hoaDon.getMaHoaDon() : "")),
                vnd(soTien),
                maGd);
        guiEmailThongBao(hoaDon, "Hoàn tiền thành công", "Hoàn tiền thành công", noiDung);
    }

    /** Gui mot email thong bao don gian (khung SUNOVA) toi khach cua don hang. */
    private void guiEmailThongBao(HoaDon hoaDon, String subject, String heading, String noiDungHtml) {
        try {
            if (hoaDon == null) {
                return;
            }
            String toEmail = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getEmail() : null;
            if (toEmail == null || toEmail.isBlank()) {
                log.warn("[MAIL] Đơn {} không có email khách, bỏ qua gửi thông báo '{}'.",
                        hoaDon.getMaHoaDon(), subject);
                return;
            }
            String html = buildNotificationHtml(hoaDon, heading, noiDungHtml);
            if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
                log.warn("[MAIL-DEV] Mail chưa cấu hình. Bỏ qua gửi '{}' đơn {} tới {}.",
                        subject, hoaDon.getMaHoaDon(), toEmail);
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(String.format("%s <%s>", fromName, fromEmail));
            helper.setTo(toEmail);
            helper.setSubject(subject + " — SUNOVA");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[MAIL] Đã gửi '{}' đơn {} tới {}", subject, hoaDon.getMaHoaDon(), toEmail);
        } catch (Exception ex) {
            log.error("[MAIL] Lỗi gửi email thông báo: {}", ex.getMessage(), ex);
        }
    }

    private String buildNotificationHtml(HoaDon hoaDon, String heading, String noiDungHtml) {
        String tenNguoiNhan = safe(hoaDon.getTenNguoiNhan());
        String greeting = tenNguoiNhan.isBlank()
                ? (hoaDon.getIdKhachHang() != null ? safe(hoaDon.getIdKhachHang().getHoTen()) : "")
                : tenNguoiNhan;
        String trackingUrl = buildTrackingUrl(safe(hoaDon.getMaHoaDon()));
        return """
                <div style="background:#f4f5f7;padding:24px 0;font-family:Segoe UI,Roboto,Arial,sans-serif;">
                  <div style="max-width:620px;margin:0 auto;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e6e8ec;">
                    <div style="background:linear-gradient(135deg,#f59e0b,#f97316);padding:24px 28px;color:#fff;">
                      <div style="font-size:22px;font-weight:800;letter-spacing:1px;">SUNOVA</div>
                      <div style="opacity:.95;margin-top:4px;">%s</div>
                    </div>
                    <div style="padding:24px 28px;">
                      <p style="margin:0 0 14px;color:#1f2430;font-size:16px;">Xin chào <strong>%s</strong>,</p>
                      %s
                      <div style="text-align:center;margin:26px 0 8px;">
                        <a href="%s" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:12px 26px;border-radius:10px;font-weight:700;">
                          Xem đơn hàng
                        </a>
                      </div>
                    </div>
                    <div style="background:#fafafa;padding:16px 28px;color:#9aa0aa;font-size:12px;text-align:center;border-top:1px solid #eef0f3;">
                      Email được gửi tự động, vui lòng không trả lời. © SUNOVA
                    </div>
                  </div>
                </div>
                """.formatted(
                esc(heading),
                esc(greeting.isBlank() ? "bạn" : greeting),
                noiDungHtml,
                trackingUrl);
    }

    private String buildInvoiceHtml(HoaDon hoaDon, List<HoaDonChiTietResponse> lines) {
        String maHoaDon = safe(hoaDon.getMaHoaDon());
        String ngay = hoaDon.getNgayTao() != null
                ? hoaDon.getNgayTao().format(DATE_FMT)
                : LocalDateTime.now().format(DATE_FMT);
        String tenNguoiNhan = safe(hoaDon.getTenNguoiNhan());
        String sdt = safe(hoaDon.getSdtNguoiNhan());
        String diaChi = safe(hoaDon.getDiaChiGiao());
        String phuongThuc = hoaDon.getIdPhuongThucThanhToan() != null
                ? safe(hoaDon.getIdPhuongThucThanhToan().getTen())
                : "—";
        String trackingUrl = buildTrackingUrl(maHoaDon);

        StringBuilder rows = new StringBuilder();
        for (HoaDonChiTietResponse line : lines) {
            String ten = safe(line.getTenSanPham());
            String bienThe = line.getBienThe() != null && !line.getBienThe().isBlank()
                    ? "<div style=\"color:#8a8f98;font-size:12px;margin-top:2px;\">" + esc(line.getBienThe()) + "</div>"
                    : "";
            rows.append("<tr>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #eee;\">")
                    .append("<div style=\"font-weight:600;color:#1f2430;\">").append(esc(ten)).append("</div>")
                    .append(bienThe)
                    .append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #eee;text-align:center;color:#4b5563;\">")
                    .append(line.getSoLuong() != null ? line.getSoLuong() : 0)
                    .append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #eee;text-align:right;color:#4b5563;\">")
                    .append(vnd(line.getDonGia()))
                    .append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #eee;text-align:right;font-weight:600;color:#1f2430;\">")
                    .append(vnd(line.getThanhTien()))
                    .append("</td>")
                    .append("</tr>");
        }

        return """
                <div style="background:#f4f5f7;padding:24px 0;font-family:Segoe UI,Roboto,Arial,sans-serif;">
                  <div style="max-width:620px;margin:0 auto;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e6e8ec;">
                    <div style="background:linear-gradient(135deg,#f59e0b,#f97316);padding:24px 28px;color:#fff;">
                      <div style="font-size:22px;font-weight:800;letter-spacing:1px;">SUNOVA</div>
                      <div style="opacity:.95;margin-top:4px;">Hóa đơn điện tử — Xác nhận đặt hàng</div>
                    </div>

                    <div style="padding:24px 28px;">
                      <p style="margin:0 0 4px;color:#1f2430;font-size:16px;">Xin chào <strong>%s</strong>,</p>
                      <p style="margin:0 0 18px;color:#4b5563;line-height:1.6;">
                        Cảm ơn bạn đã mua sắm tại SUNOVA. Đơn hàng của bạn đã được ghi nhận thành công.
                        Dưới đây là thông tin hóa đơn điện tử.
                      </p>

                      <table style="width:100%%;border-collapse:collapse;margin-bottom:18px;">
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Mã đơn hàng</td>
                          <td style="padding:4px 0;text-align:right;font-weight:700;color:#f97316;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Ngày đặt</td>
                          <td style="padding:4px 0;text-align:right;color:#1f2430;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Phương thức thanh toán</td>
                          <td style="padding:4px 0;text-align:right;color:#1f2430;">%s</td>
                        </tr>
                      </table>

                      <div style="background:#f9fafb;border:1px solid #eef0f3;border-radius:10px;padding:14px 16px;margin-bottom:18px;">
                        <div style="font-weight:700;color:#1f2430;margin-bottom:6px;">Người nhận</div>
                        <div style="color:#4b5563;line-height:1.6;">
                          %s &nbsp;•&nbsp; %s<br/>%s
                        </div>
                      </div>

                      <table style="width:100%%;border-collapse:collapse;">
                        <thead>
                          <tr style="background:#fafafa;">
                            <th style="padding:10px 8px;text-align:left;color:#8a8f98;font-size:12px;text-transform:uppercase;">Sản phẩm</th>
                            <th style="padding:10px 8px;text-align:center;color:#8a8f98;font-size:12px;text-transform:uppercase;">SL</th>
                            <th style="padding:10px 8px;text-align:right;color:#8a8f98;font-size:12px;text-transform:uppercase;">Đơn giá</th>
                            <th style="padding:10px 8px;text-align:right;color:#8a8f98;font-size:12px;text-transform:uppercase;">Thành tiền</th>
                          </tr>
                        </thead>
                        <tbody>%s</tbody>
                      </table>

                      <table style="width:100%%;border-collapse:collapse;margin-top:14px;">
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Tạm tính</td>
                          <td style="padding:4px 0;text-align:right;color:#1f2430;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Giảm giá</td>
                          <td style="padding:4px 0;text-align:right;color:#16a34a;">- %s</td>
                        </tr>
                        <tr>
                          <td style="padding:4px 0;color:#8a8f98;">Phí vận chuyển</td>
                          <td style="padding:4px 0;text-align:right;color:#1f2430;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:12px 0 0;font-weight:800;color:#1f2430;font-size:16px;border-top:2px solid #f1f1f1;">Tổng thanh toán</td>
                          <td style="padding:12px 0 0;text-align:right;font-weight:800;color:#f97316;font-size:18px;border-top:2px solid #f1f1f1;">%s</td>
                        </tr>
                      </table>

                      <div style="text-align:center;margin:26px 0 8px;">
                        <a href="%s" style="display:inline-block;background:#f97316;color:#fff;text-decoration:none;padding:12px 26px;border-radius:10px;font-weight:700;">
                          Theo dõi đơn hàng
                        </a>
                      </div>
                      <p style="text-align:center;color:#8a8f98;font-size:12px;margin:6px 0 0;">
                        Hoặc tra cứu với mã đơn <strong>%s</strong> tại website SUNOVA.
                      </p>
                    </div>

                    <div style="background:#fafafa;padding:16px 28px;color:#9aa0aa;font-size:12px;text-align:center;border-top:1px solid #eef0f3;">
                      Email được gửi tự động, vui lòng không trả lời. © SUNOVA
                    </div>
                  </div>
                </div>
                """.formatted(
                esc(tenNguoiNhan.isBlank() ? "bạn" : tenNguoiNhan),
                esc(maHoaDon),
                esc(ngay),
                esc(phuongThuc),
                esc(tenNguoiNhan),
                esc(sdt),
                esc(diaChi),
                rows,
                vnd(hoaDon.getTongTien()),
                vnd(hoaDon.getTienGiamGia()),
                vnd(hoaDon.getPhiVanChuyen()),
                vnd(hoaDon.getThanhTien()),
                trackingUrl,
                esc(maHoaDon));
    }

    private String buildTrackingUrl(String maHoaDon) {
        String base = frontendBaseUrl != null && !frontendBaseUrl.isBlank()
                ? frontendBaseUrl.trim().replaceAll("/+$", "")
                : "http://localhost:5173";
        return base + "/tra-cuu-don?ma=" + URLEncoder.encode(maHoaDon, StandardCharsets.UTF_8);
    }

    private String vnd(BigDecimal value) {
        BigDecimal v = value != null ? value : BigDecimal.ZERO;
        String s = v.setScale(0, java.math.RoundingMode.HALF_UP).toBigInteger().toString();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
            if (++count % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return sb.reverse() + "\u20ab";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
