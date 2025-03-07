# Sending Email Using Spring Boot 3

## Introduction

Sending emails is a common requirement in modern applications, whether for user notifications, password resets, or confirmations. In this guide, we will explore how to send emails using Spring Boot 3 with JavaMailSender.

## Prerequisites

- Java 21 installed
- A working SMTP server credentials (e.g., Gmail, Mailtrap, or another SMTP provider)

## Dependencies

To enable email sending, we need to include the following dependencies in our `pom.xml` file:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

The `spring-boot-starter-mail` dependency provides the necessary components to send emails, while `spring-boot-starter-web` allows us to expose a REST API for sending emails.

---

## Configuration

We need to configure our email settings in `application.yml`:

```yaml
spring:
  application:
    name: spring-mail-sender
  mail:
    host: ${MAIL_HOST}
    username: ${SPRING_MAIL_USERNAME}
    password: ${SPRING_MAIL_PASSWORD}
    port: ${MAIL_PORT}
```

Make sure to replace these placeholders with your actual SMTP credentials.

---

## Mail Configuration Class

We will create a configuration class to set up the JavaMailSender bean.

### `MailConfig.java`

```java
package com.mahmud.springmailsender.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration // Marks this class as a Spring configuration class
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
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    @Bean // Defines the JavaMailSender bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(this.port);
        mailSender.setUsername(this.username);
        mailSender.setPassword(this.password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
```

### Breakdown:

- The `@Configuration` annotation makes this a configuration class.
- The `@Value` annotations inject email properties from `application.yml`.
- The `javaMailSender()` method configures the mail sender, enabling SMTP authentication and TLS.

---

## Data Transfer Objects (DTOs)

We define DTOs to handle email requests and responses.

### `EmailRequest.java`

```java
package com.mahmud.springmailsender.dto;

public record EmailRequest(
        String to,
        String subject,
        String body) {
}
```

### `EmailResponse.java`

```java
package com.mahmud.springmailsender.dto;

public record EmailResponse(
        String message,
        Boolean ok) {
}
```

### Breakdown:

- `EmailRequest` represents the incoming email request.
- `EmailResponse` represents the API response after sending an email.

---

## Email Service

### `EmailService.java`

```java
package com.mahmud.springmailsender.service;

import com.mahmud.springmailsender.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service // Marks this class as a service component
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final String mailUsername;

    public EmailService(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String mailUsername) {
        this.javaMailSender = javaMailSender;
        this.mailUsername = mailUsername;
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
```

### Breakdown:

- The `@Service` annotation makes this a Spring service.
- The `sendEmail()` method creates and sends a simple email message.

---

## REST API Controller

### `EmailController.java`

```java
package com.mahmud.springmailsender.controller;

import com.mahmud.springmailsender.dto.EmailRequest;
import com.mahmud.springmailsender.dto.EmailResponse;
import com.mahmud.springmailsender.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest) {
        emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(new EmailResponse("Email sent successfully", true));
    }
}
```

### Breakdown:

- The `@RestController` annotation marks this as a REST API controller.
- The `@PostMapping("/send")` endpoint sends an email using the `EmailService`.



## Run the project

Open your terminal and run the following command to start our Spring Boot project.

```bash
./mvnw spring-boot:run
```

---

## Testing the API with cURL

To test the email sending functionality, run the Spring Boot application and use the following cURL command:

```bash
curl -X POST http://localhost:8080/api/email/send \
     -H "Content-Type: application/json" \
     -d '{"to":"recipient@example.com", "subject":"Test Email", "body":"Hello, this is a test email."}'
```

If successful, you will receive a response:

```json
{
    "message": "Email sent successfully",
    "ok": true
}
```

---

## Conclusion

In this article, we covered how to send emails using Spring Boot 3 with JavaMailSender. We created a configuration class, a service, and a REST API to send emails. Now, you can integrate this feature into your Spring Boot application!

Happy coding! 🚀

