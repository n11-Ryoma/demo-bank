package com.example.ebank.security.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.ebank.security.dto.LoginHistoryItem;
import com.example.ebank.security.dto.SessionItem;
import com.example.ebank.security.repository.jdbc.SecuritySessionRepositoryJdbc;

@Service
public class SecuritySessionService {

    private static final int MAX_LIMIT = 100;

    private final SecuritySessionRepositoryJdbc repository;

    public SecuritySessionService(SecuritySessionRepositoryJdbc repository) {
        this.repository = repository;
    }

    public void recordLoginSuccess(Long userId, String token, String ip, String userAgent) {
        if (userId == null || token == null || token.isBlank()) {
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        repository.insertSession(sessionId, userId, token, ip, userAgent);
        repository.insertLoginHistory(userId, "SUCCESS", ip, userAgent);
    }

    public void recordLoginFailure(Long userId, String ip, String userAgent) {
        if (userId == null) {
            return;
        }
        repository.insertLoginHistory(userId, "FAILED", ip, userAgent);
    }

    public List<LoginHistoryItem> getLoginHistory(Long userId, int limit) {
        int bounded = Math.max(1, Math.min(limit, MAX_LIMIT));
        return repository.findLoginHistory(userId, bounded);
    }

    public List<SessionItem> getSessions(Long userId, String currentToken) {
        List<SessionItem> list = repository.findSessions(userId);
        String currentSessionId = repository.findSessionIdByTokenAndUserId(currentToken, userId).orElse("");
        for (SessionItem item : list) {
            item.setCurrent(item.getSessionId().equals(currentSessionId));
        }
        return list;
    }

    public boolean logoutCurrent(Long userId, String token) {
        return repository.deleteSessionByToken(userId, token) > 0;
    }

    public boolean logoutBySessionId(Long userId, String sessionId) {
        return repository.deleteSessionBySessionId(userId, sessionId) > 0;
    }

    public Optional<Long> getUserIdByToken(String token) {
        return repository.findUserIdByToken(token);
    }
}
