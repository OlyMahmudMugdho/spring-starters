package com.mahmud.springmailsender.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    public MailConfig(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.port}") Integer port
    ) {
        System.out.println();
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(this.port);
        mailSender.setUsername(this.username);
        mailSender.setPassword(this.password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");  // Critical: Enable STARTTLS
        props.put("mail.smtp.starttls.required", "true"); // Optional: Enforce STARTTLS
        props.put("mail.debug", "true");

        return mailSender;
    }
}
