package org.example.templatejava6.staff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email thông báo khi mật khẩu nhân viên bị đổi / đặt lại.
 * Không bao giờ gửi mật khẩu trong nội dung mail.
 */
@Service
public class NhanVienMatKhauMailService {

    private static final Logger log = LoggerFactory.getLogger(NhanVienMatKhauMailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:SUNOVA}")
    private String fromName;

    /** Nhân viên tự đổi mật khẩu. */
    public void guiThongBaoTuDoi(String toEmail, String hoTen, String thoiGian) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[NV-MK] Bỏ qua mail tự đổi: tài khoản {} không có email", hoTen);
            return;
        }
        String subject = "Mật khẩu tài khoản SUNOVA vừa được đổi";
        String body = """
                Xin chào %s,

                Mật khẩu tài khoản SUNOVA của bạn vừa được đổi lúc %s.

                Nếu không phải bạn, hãy báo ngay cho chủ cửa hàng.

                Trân trọng,
                SUNOVA
                """.formatted(displayName(hoTen), thoiGian);
        sendSafe(toEmail, subject, body);
    }

    /** Quản lý/chủ đặt lại hoặc sinh mật khẩu tạm. */
    public void guiThongBaoBiDatLai(
            String toEmail,
            String hoTen,
            String nguoiThucHien,
            String thoiGian) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[NV-MK] Bỏ qua mail đặt lại: tài khoản {} không có email", hoTen);
            return;
        }
        String subject = "Mật khẩu tài khoản SUNOVA vừa được đặt lại";
        String body = """
                Xin chào %s,

                Mật khẩu tài khoản SUNOVA của bạn vừa được đặt lại bởi %s lúc %s.

                Hãy đăng nhập và đổi sang mật khẩu mới của riêng bạn.
                Nếu bạn không yêu cầu, hãy báo ngay cho chủ cửa hàng.

                Trân trọng,
                SUNOVA
                """.formatted(displayName(hoTen), nguoiThucHien, thoiGian);
        sendSafe(toEmail, subject, body);
    }

    private void sendSafe(String toEmail, String subject, String body) {
        try {
            if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
                log.warn("[NV-MK-DEV] Mail chưa cấu hình. Subject=\"{}\" → {}", subject, toEmail);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromEmail));
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[NV-MK] Đã gửi email thông báo tới {}", toEmail);
        } catch (Exception ex) {
            log.error("[NV-MK] Gửi email thất bại tới {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    private static String displayName(String hoTen) {
        return hoTen != null && !hoTen.isBlank() ? hoTen.trim() : "bạn";
    }
}
