package com.fitlife.mail.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/mail")
@RequiredArgsConstructor
public class TestMailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ApiResponse<Void> sendTestMail(
            @RequestParam String to
    ) {
        emailService.sendSimpleMail(
                to,
                "FitLife Test Email",
                "This is a test email from FitLife backend."
        );

        return ApiResponse.success("Test email sent successfully");
    }
}