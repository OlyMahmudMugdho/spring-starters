package com.mahmud.redispostgres.service;

import com.mahmud.redispostgres.model.Session;
import com.mahmud.redispostgres.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(Long userId) {
        Session session = new Session();
        session.setUserId(userId);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActiveTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public Session getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    public Iterable<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public void updateLastActiveTime(String sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setLastActiveTime(LocalDateTime.now());
            sessionRepository.save(session);
        }
    }

    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
