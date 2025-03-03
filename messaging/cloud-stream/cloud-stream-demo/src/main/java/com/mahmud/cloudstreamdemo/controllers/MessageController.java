package com.mahmud.cloudstreamdemo.controllers;

import com.mahmud.cloudstreamdemo.models.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    private StreamBridge streamBridge;

    @PostMapping("/send")
    public String sendMessage(@RequestBody Greeting greeting) {
        streamBridge.send("produceMessage-out-0", greeting);
        return "Message sent: " + greeting;
    }
}
