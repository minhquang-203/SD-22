package org.example.templatejava6.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class KhachOtpEmailService {

    private static final Logger log = LoggerFactory.getLogger(KhachOtpEmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:SUNOVA}")
    private String fromName;

    public void sendOtp(String toEmail, String hoTen, String otp) {
        String subject = "Mã xác nhận đặt lại mật khẩu — SUNOVA";
        String body = buildBody(hoTen, otp);

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("[OTP-DEV] Mail chưa cấu hình. Gửi OTP tới {}: {}", toEmail, otp);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.format("%s <%s>", fromName, fromEmail));
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildBody(String hoTen, String otp) {
        String name = hoTen != null && !hoTen.isBlank() ? hoTen : "bạn";
        return """
                Xin chào %s,

                Bạn vừa yêu cầu đặt lại mật khẩu tài khoản SUNOVA.

                Mã xác nhận (OTP): %s

                Mã có hiệu lực trong 15 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.

                Nếu bạn không yêu cầu, hãy bỏ qua email này.

                Trân trọng,
                SUNOVA
                """.formatted(name, otp);
    }
}
