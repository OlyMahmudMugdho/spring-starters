package com.mahmud.customexceptionrestapi.exceptions;


import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final String STATUS = "USER_NOT_FOUND";
    public UserNotFoundException(String message) {
        super(message);
    }
}
