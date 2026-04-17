package com.fitlife.core.mail.impl;

import com.fitlife.core.mail.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String htmlBody = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px;'>"
                    + "<h1 style='color: #007bff;'>Chào mừng " + fullName + " đến với FitLife Gym!</h1>"
                    + "<p>Chúng tôi rất vui mừng khi bạn đã lựa chọn FitLife để bắt đầu hành trình thay đổi bản thân.</p>"
                    + "<p>Tài khoản của bạn đã được khởi tạo thành công. Hãy đăng nhập ngay để khám phá các lộ trình tập luyện xịn xò nhé!</p>"
                    + "<br><a href='#' style='background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Bắt đầu tập luyện ngay</a>"
                    + "<p style='font-size: 12px; color: #6c757d; margin-top: 20px;'>Đội ngũ FitLife Support.</p>"
                    + "</div></body></html>";

            helper.setTo(toEmail);
            helper.setSubject("🔥 Chào mừng thành viên mới của FitLife!");
            helper.setText(htmlBody, true);
            helper.setFrom("fitlife-system@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String htmlBody = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px; border: 1px solid #ddd;'>"
                    + "<h2 style='color: #dc3545;'>Yêu cầu khôi phục mật khẩu</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản liên kết với email này.</p>"
                    + "<p>Mã OTP của bạn là: <strong style='font-size: 24px; color: #007bff; letter-spacing: 5px;'>" + otp + "</strong></p>"
                    + "<p style='color: red;'><i>* Mã này sẽ hết hạn trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</i></p>"
                    + "<p style='font-size: 12px; color: #6c757d; margin-top: 20px;'>Nếu bạn không yêu cầu đổi mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div></body></html>";

            helper.setTo(toEmail);
            helper.setSubject("🔒 Mã OTP khôi phục mật khẩu FitLife");
            helper.setText(htmlBody, true);
            helper.setFrom("fitlife-security@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi email OTP: " + e.getMessage());
        }
    }
}