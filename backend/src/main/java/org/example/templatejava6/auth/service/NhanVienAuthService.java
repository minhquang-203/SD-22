package org.example.templatejava6.auth.service;

import org.example.templatejava6.auth.model.request.DangNhapRequest;
import org.example.templatejava6.auth.model.response.AuthResponse;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.security.JwtTokenProvider;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NhanVienAuthService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse dangNhap(DangNhapRequest request) {
        String taiKhoan = request.getTaiKhoan().trim();
        NhanVien nv = nhanVienRepository.findByEmailOrSoDienThoai(taiKhoan)
                .orElseThrow(() -> new ApiException("Email/SĐT hoặc mật khẩu không đúng", "AUTH_FAILED"));

        if (!Boolean.TRUE.equals(nv.getTrangThai())) {
            throw new ApiException("Tài khoản đã bị khóa", "ACCOUNT_DISABLED");
        }

        if (nv.getVaiTro() == null) {
            throw new ApiException("Tài khoản chưa được gán vai trò", "CONFIG_ERROR");
        }

        String maVaiTro = nv.getVaiTro().getMaVaiTro();
        if (!"NHAN_VIEN".equals(maVaiTro) && !"QUAN_LY".equals(maVaiTro) && !"CHU".equals(maVaiTro)) {
            throw new ApiException("Tài khoản không có quyền truy cập hệ thống quản trị", "AUTH_FAILED");
        }

        if (!matchesPassword(request.getMatKhau(), nv.getMatKhau())) {
            throw new ApiException("Email/SĐT hoặc mật khẩu không đúng", "AUTH_FAILED");
        }

        String token = jwtTokenProvider.createToken(nv.getId(), nv.getHoTen(), maVaiTro);
        return new AuthResponse(nv.getId(), token, nv.getHoTen(), maVaiTro);
    }

    private boolean matchesPassword(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }
}
