package com.fitlife.common.mail.impl;

import com.fitlife.common.mail.EmailService;
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
                    + "<h1 style='color: #007bff;'>ChĂ o má»«ng " + fullName + " Ä‘áº¿n vá»›i FitLife Gym!</h1>"
                    + "<p>ChĂºng tĂ´i ráº¥t vui má»«ng khi báº¡n Ä‘Ă£ lá»±a chá»n FitLife Ä‘á»ƒ báº¯t Ä‘áº§u hĂ nh trĂ¬nh thay Ä‘á»•i báº£n thĂ¢n.</p>"
                    + "<p>TĂ i khoáº£n cá»§a báº¡n Ä‘Ă£ Ä‘Æ°á»£c khá»Ÿi táº¡o thĂ nh cĂ´ng. HĂ£y Ä‘Äƒng nháº­p ngay Ä‘á»ƒ khĂ¡m phĂ¡ cĂ¡c lá»™ trĂ¬nh táº­p luyá»‡n xá»‹n xĂ² nhĂ©!</p>"
                    + "<br><a href='#' style='background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Báº¯t Ä‘áº§u táº­p luyá»‡n ngay</a>"
                    + "<p style='font-size: 12px; color: #6c757d; margin-top: 20px;'>Äá»™i ngÅ© FitLife Support.</p>"
                    + "</div></body></html>";

            helper.setTo(toEmail);
            helper.setSubject("đŸ”¥ ChĂ o má»«ng thĂ nh viĂªn má»›i cá»§a FitLife!");
            helper.setText(htmlBody, true);
            helper.setFrom("fitlife-system@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lá»—i gá»­i email: " + e.getMessage());
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
                    + "<h2 style='color: #dc3545;'>YĂªu cáº§u khĂ´i phá»¥c máº­t kháº©u</h2>"
                    + "<p>ChĂ o báº¡n,</p>"
                    + "<p>ChĂºng tĂ´i nháº­n Ä‘Æ°á»£c yĂªu cáº§u khĂ´i phá»¥c máº­t kháº©u cho tĂ i khoáº£n liĂªn káº¿t vá»›i email nĂ y.</p>"
                    + "<p>MĂ£ OTP cá»§a báº¡n lĂ : <strong style='font-size: 24px; color: #007bff; letter-spacing: 5px;'>" + otp + "</strong></p>"
                    + "<p style='color: red;'><i>* MĂ£ nĂ y sáº½ háº¿t háº¡n trong vĂ²ng 5 phĂºt. Vui lĂ²ng khĂ´ng chia sáº» mĂ£ nĂ y cho báº¥t ká»³ ai.</i></p>"
                    + "<p style='font-size: 12px; color: #6c757d; margin-top: 20px;'>Náº¿u báº¡n khĂ´ng yĂªu cáº§u Ä‘á»•i máº­t kháº©u, vui lĂ²ng bá» qua email nĂ y.</p>"
                    + "</div></body></html>";

            helper.setTo(toEmail);
            helper.setSubject("đŸ”’ MĂ£ OTP khĂ´i phá»¥c máº­t kháº©u FitLife");
            helper.setText(htmlBody, true);
            helper.setFrom("fitlife-security@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lá»—i gá»­i email OTP: " + e.getMessage());
        }
    }
}