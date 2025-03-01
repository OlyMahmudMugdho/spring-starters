package com.mahmud.rabbitmqpojo.dto;

import java.io.Serializable;

public class Message implements Serializable {
    private Long id;
    private String sender;

    // Constructors, getters, and setters
    public Message() {}

    public Message(Long id, String sender) {
        this.id = id;
        this.sender = sender;
    }

    public Long getId() {
        return id;
    }

    public void setContent(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

