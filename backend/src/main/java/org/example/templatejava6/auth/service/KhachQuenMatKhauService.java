package org.example.templatejava6.auth.service;

import org.example.templatejava6.auth.model.request.DatLaiMatKhauRequest;
import org.example.templatejava6.auth.model.request.QuenMatKhauRequest;
import org.example.templatejava6.auth.model.response.MessageResponse;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class KhachQuenMatKhauService {

    public static final String GENERIC_SENT_MESSAGE =
            "Nếu email tồn tại trong hệ thống, mã xác nhận đã được gửi tới hộp thư của bạn.";

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private OtpRamStore otpRamStore;

    @Autowired
    private KhachOtpEmailService khachOtpEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public MessageResponse quenMatKhau(QuenMatKhauRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (otpRamStore.canSend(email)) {
            khachHangRepository.findByEmailIgnoreCase(email).ifPresent(kh -> {
                if (!Boolean.TRUE.equals(kh.getTrangThai())) {
                    return;
                }
                String otp = sinhOtp();
                otpRamStore.save(email, otp);
                khachOtpEmailService.sendOtp(kh.getEmail(), kh.getHoTen(), otp);
            });
        }

        return new MessageResponse(GENERIC_SENT_MESSAGE);
    }

    @Transactional
    public MessageResponse datLaiMatKhau(DatLaiMatKhauRequest request) {
        String email = normalizeEmail(request.getEmail());
        String otp = request.getOtp().trim();

        if (!otpRamStore.verifyAndRemove(email, otp)) {
            throw new ApiException("Mã không đúng hoặc đã hết hạn", "OTP_INVALID");
        }

        KhachHang kh = khachHangRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("Mã không đúng hoặc đã hết hạn", "OTP_INVALID"));

        if (!Boolean.TRUE.equals(kh.getTrangThai())) {
            throw new ApiException("Tài khoản đã bị khóa", "ACCOUNT_DISABLED");
        }

        kh.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        khachHangRepository.save(kh);

        return new MessageResponse("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
    }

    private String sinhOtp() {
        int value = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", value);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException("Email không hợp lệ", "VALIDATION_ERROR");
        }
        return email.trim().toLowerCase();
    }
}
