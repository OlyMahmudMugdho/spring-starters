package com.mahmud.springmailsender.controller;

import com.mahmud.springmailsender.dto.EmailRequest;
import com.mahmud.springmailsender.dto.EmailResponse;
import com.mahmud.springmailsender.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class MailController {
    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            return ResponseEntity.ok(new EmailResponse("Email sent successfully", true));
        } catch (MailException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailResponse("Failed to send email: " + e.getMessage(), false));
        }
    }
}