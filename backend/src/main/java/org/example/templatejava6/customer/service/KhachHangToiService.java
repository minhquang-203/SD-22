package org.example.templatejava6.customer.service;

import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.model.request.CapNhatKhachHangToiRequest;
import org.example.templatejava6.customer.model.request.DoiMatKhauToiRequest;
import org.example.templatejava6.customer.model.response.KhachHangToiResponse;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KhachHangToiService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public KhachHangToiResponse layThongTinToi() {
        return new KhachHangToiResponse(getKhachDangNhap());
    }

    @Transactional
    public KhachHangToiResponse capNhatThongTin(CapNhatKhachHangToiRequest request) {
        KhachHang kh = getKhachDangNhap();
        String email = request.getEmail().trim().toLowerCase();
        String sdt = request.getSoDienThoai().trim();

        if (khachHangRepository.existsByEmailIgnoreCaseAndIdNot(email, kh.getId())) {
            throw new ApiException("Email đã được sử dụng bởi tài khoản khác", "DUPLICATE_EMAIL");
        }
        if (khachHangRepository.existsBySoDienThoaiAndIdNot(sdt, kh.getId())) {
            throw new ApiException("Số điện thoại đã được sử dụng bởi tài khoản khác", "DUPLICATE_PHONE");
        }

        kh.setHoTen(request.getHoTen().trim());
        kh.setEmail(email);
        kh.setSoDienThoai(sdt);
        khachHangRepository.save(kh);
        return new KhachHangToiResponse(kh);
    }

    @Transactional
    public void doiMatKhau(DoiMatKhauToiRequest request) {
        KhachHang kh = getKhachDangNhap();
        if (!matchesPassword(request.getMatKhauCu(), kh.getMatKhau())) {
            throw new ApiException("Mật khẩu hiện tại không đúng", "WRONG_PASSWORD");
        }
        kh.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        khachHangRepository.save(kh);
    }

    private KhachHang getKhachDangNhap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new ApiException("Chưa đăng nhập", "UNAUTHORIZED");
        }
        Integer id;
        try {
            id = Integer.parseInt(auth.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy tài khoản", "NOT_FOUND"));
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
