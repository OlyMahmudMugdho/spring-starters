package com.mahmud.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class DemoController {

    @GetMapping("/secured")
    public Map<String,String> securedMethod(@AuthenticationPrincipal Jwt jwt) {
        Map<String,String> map = new HashMap<>();
        map.put("message","this is a secure message");
        map.put("username", jwt.getClaimAsString("preferred_username")); // Extract username from JWT
        return map;
    }
}
