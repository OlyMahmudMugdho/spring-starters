package com.mahmud.springmailsender.dto;

public record EmailResponse(
        String message,
        Boolean ok
) {
}
