package com.mahmud.kafkapojo.models;

import java.util.Objects;

public record UserMessage(String username, String content) {
    // Compact constructor for validation
    public UserMessage {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
    }
}
