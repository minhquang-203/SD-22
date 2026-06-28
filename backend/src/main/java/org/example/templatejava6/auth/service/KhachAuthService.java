package org.example.templatejava6.auth.service;

import org.example.templatejava6.auth.model.request.DangKyRequest;
import org.example.templatejava6.auth.model.request.DangNhapRequest;
import org.example.templatejava6.auth.model.response.AuthResponse;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.VaiTro;
import org.example.templatejava6.common.repository.VaiTroRepository;
import org.example.templatejava6.common.security.JwtTokenProvider;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class KhachAuthService {

    private static final String MA_VAI_TRO_KHACH = "KHACH_HANG";

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse dangKy(DangKyRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String sdt = request.getSoDienThoai().trim();

        if (khachHangRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException("Email đã được sử dụng", "DUPLICATE_EMAIL");
        }
        if (khachHangRepository.existsBySoDienThoai(sdt)) {
            throw new ApiException("Số điện thoại đã được sử dụng", "DUPLICATE_PHONE");
        }

        VaiTro vaiTro = vaiTroRepository.findByMaVaiTro(MA_VAI_TRO_KHACH)
                .orElseThrow(() -> new ApiException("Vai trò khách hàng chưa được cấu hình", "CONFIG_ERROR"));

        KhachHang kh = new KhachHang();
        kh.setVaiTro(vaiTro);
        kh.setMaKhachHang(sinhMaKhachHang());
        kh.setHoTen(request.getHoTen().trim());
        kh.setEmail(email);
        kh.setSoDienThoai(sdt);
        kh.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        kh.setGioiTinh("Khac");
        kh.setDiemTichLuy(0);
        kh.setTrangThai(true);
        kh.setNgayTao(LocalDateTime.now());

        khachHangRepository.save(kh);
        return buildAuthResponse(kh);
    }

    @Transactional(readOnly = true)
    public AuthResponse dangNhap(DangNhapRequest request) {
        String taiKhoan = request.getTaiKhoan().trim();
        KhachHang kh = khachHangRepository.findByEmailOrSoDienThoai(taiKhoan)
                .orElseThrow(() -> new ApiException("Email/SĐT hoặc mật khẩu không đúng", "AUTH_FAILED"));

        if (!Boolean.TRUE.equals(kh.getTrangThai())) {
            throw new ApiException("Tài khoản đã bị khóa", "ACCOUNT_DISABLED");
        }

        if (!matchesPassword(request.getMatKhau(), kh.getMatKhau())) {
            throw new ApiException("Email/SĐT hoặc mật khẩu không đúng", "AUTH_FAILED");
        }

        return buildAuthResponse(kh);
    }

    private AuthResponse buildAuthResponse(KhachHang kh) {
        String vaiTro = kh.getVaiTro() != null ? kh.getVaiTro().getMaVaiTro() : MA_VAI_TRO_KHACH;
        String token = jwtTokenProvider.createToken(kh.getId(), kh.getHoTen(), vaiTro);
        return new AuthResponse(kh.getId(), token, kh.getHoTen(), vaiTro);
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

    private String sinhMaKhachHang() {
        for (int i = 0; i < 50; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(100, 10000);
            String ma = "KH" + suffix;
            if (!khachHangRepository.existsByMaKhachHang(ma)) {
                return ma;
            }
        }
        throw new ApiException("Không thể sinh mã khách hàng. Vui lòng thử lại.", "CODE_GEN_FAILED");
    }
}
