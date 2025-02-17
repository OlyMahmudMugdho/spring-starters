package com.mahmud.redispostgres.controller;

import com.mahmud.redispostgres.model.Session;
import com.mahmud.redispostgres.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Session> createSession(@RequestParam Long userId) {
        return ResponseEntity.ok(sessionService.createSession(userId));
    }

    @GetMapping
    public ResponseEntity<Iterable<Session>> getAllSessions() {
        Iterable<Session> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Session> getSession(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{sessionId}/active")
    public ResponseEntity<Void> updateLastActiveTime(@PathVariable String sessionId) {
        sessionService.updateLastActiveTime(sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
