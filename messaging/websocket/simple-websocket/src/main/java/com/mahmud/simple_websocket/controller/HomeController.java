package com.mahmud.simple_websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public String Index() {
        return "index";
    }

    @GetMapping("/chat")
    public String Chat() {
        return "chat";
    }

}
