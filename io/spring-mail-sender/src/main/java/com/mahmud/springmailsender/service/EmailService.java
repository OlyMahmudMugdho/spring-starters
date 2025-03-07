package com.mahmud.springmailsender.service;

import com.mahmud.springmailsender.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public JavaMailSender javaMailSender;
    public String mailUsername;

    public EmailService(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String mailUsername
    ) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(EmailRequest emailRequest) throws MailException {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailUsername);
        mailMessage.setTo(emailRequest.to());
        mailMessage.setSubject(emailRequest.subject());
        mailMessage.setText(emailRequest.body());

        javaMailSender.send(mailMessage);
    }

}
