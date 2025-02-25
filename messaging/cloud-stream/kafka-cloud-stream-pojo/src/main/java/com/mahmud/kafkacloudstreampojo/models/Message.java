package com.mahmud.kafkacloudstreampojo.models;

import java.time.LocalDateTime;

public class Message {
    private String content;
    private LocalDateTime timestamp;

    // Default constructor (required for deserialization)
    public Message() {}

    // Parameterized constructor
    public Message(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
