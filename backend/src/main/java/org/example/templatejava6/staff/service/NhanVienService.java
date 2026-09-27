package org.example.templatejava6.staff.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.VaiTro;
import org.example.templatejava6.common.repository.VaiTroRepository;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.staff.model.request.DoiMatKhauNhanVienRequest;
import org.example.templatejava6.staff.model.request.NhanVienCreateRequest;
import org.example.templatejava6.staff.model.request.NhanVienDatLaiMatKhauRequest;
import org.example.templatejava6.staff.model.request.NhanVienTrangThaiRequest;
import org.example.templatejava6.staff.model.request.NhanVienUpdateRequest;
import org.example.templatejava6.staff.model.response.DatLaiMatKhauResponse;
import org.example.templatejava6.staff.model.response.NhanVienResponse;
import org.example.templatejava6.staff.util.VaiTroRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NhanVienService {

    private static final Logger log = LoggerFactory.getLogger(NhanVienService.class);
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NhanVienMatKhauMailService matKhauMailService;

    @Transactional(readOnly = true)
    public List<NhanVienResponse> danhSachHoatDong() {
        return nhanVienRepository.findActiveWithVaiTro().stream()
                .map(NhanVienResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NhanVienResponse> danhSachQuanLy() {
        return nhanVienRepository.findAllWithVaiTro().stream()
                .map(NhanVienResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public NhanVienResponse chiTiet(Integer id) {
        NhanVien actor = currentActor();
        NhanVien target = resolveNhanVien(id);
        if (!VaiTroRank.isSelf(actor, target)) {
            VaiTroRank.assertCanManage(actor, target);
        }
        return new NhanVienResponse(target);
    }

    @Transactional
    public NhanVienResponse taoMoi(NhanVienCreateRequest request) {
        NhanVien actor = currentActor();
        VaiTroRank.assertCanAssignRole(actor, request.getMaVaiTro());

        String email = normalizeEmail(request.getEmail());
        String sdt = normalizePhone(request.getSoDienThoai());
        validateUniqueContact(email, sdt, null);

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(sinhMaNhanVien());
        nv.setHoTen(request.getHoTen().trim());
        nv.setEmail(email);
        nv.setSoDienThoai(sdt);
        nv.setMatKhau(request.getMatKhau());
        nv.setVaiTro(resolveVaiTro(request.getMaVaiTro()));
        nv.setGioiTinh(request.getGioiTinh());
        nv.setNgayVaoLam(request.getNgayVaoLam());
        nv.setTrangThai(true);

        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    @Transactional
    public NhanVienResponse capNhat(Integer id, NhanVienUpdateRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        boolean self = VaiTroRank.isSelf(actor, nv);

        if (self) {
            VaiTroRank.assertSelfKeepsRole(nv, request.getMaVaiTro());
        } else {
            VaiTroRank.assertCanManage(actor, nv);
            VaiTroRank.assertCanAssignRole(actor, request.getMaVaiTro());
            nv.setVaiTro(resolveVaiTro(request.getMaVaiTro()));
        }

        String email = normalizeEmail(request.getEmail());
        String sdt = normalizePhone(request.getSoDienThoai());
        validateUniqueContact(email, sdt, id);

        nv.setHoTen(request.getHoTen().trim());
        nv.setEmail(email);
        nv.setSoDienThoai(sdt);
        nv.setGioiTinh(request.getGioiTinh());
        nv.setNgayVaoLam(request.getNgayVaoLam());

        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    @Transactional
    public NhanVienResponse doiTrangThai(Integer id, NhanVienTrangThaiRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        VaiTroRank.assertCanManage(actor, nv);
        nv.setTrangThai(request.getTrangThai());
        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    /** Nhân viên / quản lý / chủ tự đổi mật khẩu (từ JWT). */
    @Transactional
    public void doiMatKhauToi(DoiMatKhauNhanVienRequest request) {
        NhanVien nv = currentActor();
        if (!matchesPassword(request.getMatKhauCu(), nv.getMatKhau())) {
            throw new ApiException("Mật khẩu hiện tại không đúng", "WRONG_PASSWORD");
        }
        String moi = request.getMatKhauMoi() != null ? request.getMatKhauMoi().trim() : "";
        if (moi.length() < 6) {
            throw new ApiException("Mật khẩu mới phải từ 6 đến 100 ký tự", "VALIDATION_ERROR");
        }
        if (matchesPassword(moi, nv.getMatKhau())) {
            throw new ApiException("Mật khẩu mới phải khác mật khẩu hiện tại", "VALIDATION_ERROR");
        }

        // Giữ cách lưu hiện có (giống đặt lại MK) — không đổi encode.
        nv.setMatKhau(moi);
        nhanVienRepository.save(nv);

        String thoiGian = nowVnFormatted();
        log.info(
                "[NV-MK] Tự đổi mật khẩu | người thực hiện={} ({}) | tài khoản bị đổi={} id={} | lúc={}",
                nv.getHoTen(),
                maVaiTroLabel(nv),
                nv.getEmail(),
                nv.getId(),
                thoiGian);

        matKhauMailService.guiThongBaoTuDoi(nv.getEmail(), nv.getHoTen(), thoiGian);
    }

    @Transactional
    public DatLaiMatKhauResponse datLaiMatKhau(Integer id, NhanVienDatLaiMatKhauRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        if (!VaiTroRank.isSelf(actor, nv)) {
            VaiTroRank.assertCanManage(actor, nv);
        }

        String raw;
        String matKhauTam = null;

        if (Boolean.TRUE.equals(request.getSinhTuDong())) {
            raw = sinhMatKhauTam();
            matKhauTam = raw;
        } else {
            raw = request.getMatKhauMoi();
            if (raw == null || raw.isBlank()) {
                throw new ApiException("Mật khẩu mới là bắt buộc", "VALIDATION_ERROR");
            }
            if (raw.length() < 6) {
                throw new ApiException("Mật khẩu phải từ 6 đến 100 ký tự", "VALIDATION_ERROR");
            }
        }

        nv.setMatKhau(raw);
        nhanVienRepository.save(nv);

        String thoiGian = nowVnFormatted();
        String nguoiThucHien = actor.getHoTen() + " – " + maVaiTroLabel(actor);
        log.info(
                "[NV-MK] Đặt lại mật khẩu | người thực hiện={} | tài khoản bị đổi={} id={} | lúc={}",
                nguoiThucHien,
                nv.getEmail(),
                nv.getId(),
                thoiGian);

        matKhauMailService.guiThongBaoBiDatLai(nv.getEmail(), nv.getHoTen(), nguoiThucHien, thoiGian);

        String message = "Đã đặt lại mật khẩu cho " + nv.getHoTen();
        return new DatLaiMatKhauResponse(message, matKhauTam);
    }

    private NhanVien currentActor() {
        return resolveNhanVien(SecurityUtils.currentNhanVienId());
    }

    private String sinhMatKhauTam() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(10);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private NhanVien resolveNhanVien(Integer id) {
        return nhanVienRepository.findByIdWithVaiTro(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));
    }

    private VaiTro resolveVaiTro(String maVaiTro) {
        return vaiTroRepository.findByMaVaiTro(maVaiTro)
                .orElseThrow(() -> new ApiException("Vai trò không hợp lệ", "VALIDATION_ERROR"));
    }

    private void validateUniqueContact(String email, String sdt, Integer excludeId) {
        if (excludeId == null) {
            if (nhanVienRepository.existsByEmailIgnoreCase(email)) {
                throw new ApiException("Email đã được sử dụng", "DUPLICATE");
            }
            if (nhanVienRepository.existsBySoDienThoai(sdt)) {
                throw new ApiException("Số điện thoại đã được sử dụng", "DUPLICATE");
            }
            return;
        }

        if (nhanVienRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId)) {
            throw new ApiException("Email đã được sử dụng", "DUPLICATE");
        }
        if (nhanVienRepository.existsBySoDienThoaiAndIdNot(sdt, excludeId)) {
            throw new ApiException("Số điện thoại đã được sử dụng", "DUPLICATE");
        }
    }

    private String sinhMaNhanVien() {
        List<String> existing = nhanVienRepository.findAllMaNhanVien();
        return MaGenerator.nextCode("NV", existing);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException("Email là bắt buộc", "VALIDATION_ERROR");
        }
        return email.trim().toLowerCase();
    }

    private String normalizePhone(String sdt) {
        if (sdt == null || sdt.isBlank()) {
            throw new ApiException("Số điện thoại là bắt buộc", "VALIDATION_ERROR");
        }
        return sdt.trim();
    }

    /** Giống NhanVienAuthService — hỗ trợ cả bcrypt lẫn plain hiện có. */
    private boolean matchesPassword(String raw, String stored) {
        if (stored == null || raw == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }

    private static String nowVnFormatted() {
        return LocalDateTime.now(VN_ZONE).format(TIME_FMT);
    }

    private static String maVaiTroLabel(NhanVien nv) {
        if (nv.getVaiTro() == null) {
            return "—";
        }
        String ten = nv.getVaiTro().getTenVaiTro();
        if (ten != null && !ten.isBlank()) {
            return ten.trim();
        }
        String ma = nv.getVaiTro().getMaVaiTro();
        if ("CHU".equalsIgnoreCase(ma)) return "Chủ";
        if ("QUAN_LY".equalsIgnoreCase(ma)) return "Quản lý";
        if ("NHAN_VIEN".equalsIgnoreCase(ma)) return "Nhân viên";
        return ma != null ? ma : "—";
    }
}
