package com.mahmud.customexceptionrestapi.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String,String> response = new HashMap<>();
        response.put("status", ex.getSTATUS());
        response.put("message", ex.getMessage());
        return ResponseEntity.status(404).body(response);
    }
}
