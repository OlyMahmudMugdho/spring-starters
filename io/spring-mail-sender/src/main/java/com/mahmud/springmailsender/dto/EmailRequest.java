package com.mahmud.springmailsender.dto;

public record EmailRequest(
        String to,
        String subject,
        String body)
{
}
