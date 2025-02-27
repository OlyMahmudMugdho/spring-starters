package com.mahmud.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";  // Thymeleaf template
    }

    @GetMapping("/public")
    public String publicPage(Model model) {
        model.addAttribute("message", "This is a public page!");
        return "public";
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal OidcUser user) {
        model.addAttribute("username", user.getPreferredUsername());
        return "home";  // Thymeleaf template
    }
}
