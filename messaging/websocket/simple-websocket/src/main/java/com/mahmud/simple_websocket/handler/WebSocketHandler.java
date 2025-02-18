package com.mahmud.simple_websocket.handler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.mahmud.simple_websocket.service.WebSocketService;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    public final WebSocketService webSocketService;

    public WebSocketHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    // executed when a connection is established
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketService.addSession(session);
        System.out.println("New connection: " + session.getId());
    }


    // sends message
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received message from " + session.getId() + ": " + payload);

        // Broadcast the message to all connected clients
        webSocketService.broadcastMessage(payload, session);
    }

    // executes when a connection is closed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from the list when a client disconnects
        webSocketService.removeSession(session);
        System.out.println("Connection closed: " + session.getId());
    }

    
}