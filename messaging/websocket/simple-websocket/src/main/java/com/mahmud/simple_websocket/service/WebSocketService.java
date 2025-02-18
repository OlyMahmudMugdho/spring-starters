package com.mahmud.simple_websocket.service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class WebSocketService {

    // Thread-safe list to store active WebSocket sessions
    private final static CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();


    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }


    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    // Broadcast a message to all connected clients
    public  void broadcastMessage(String message, WebSocketSession senderSession) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(senderSession.getId() + " : "  +  message));
            }
        }
    }
}

