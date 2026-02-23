package com.example.ebank.security.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.example.ebank.security.dto.LoginHistoryItem;
import com.example.ebank.security.dto.SessionItem;
import com.example.ebank.security.repository.jdbc.SecuritySessionRepositoryJdbc;

@Service
public class SecuritySessionService {

    private static final Logger log = LogManager.getLogger(SecuritySessionService.class);
    private static final int MAX_LIMIT = 100;

    private final SecuritySessionRepositoryJdbc repository;

    public SecuritySessionService(SecuritySessionRepositoryJdbc repository) {
        this.repository = repository;
    }

    public void recordLoginSuccess(Long userId, String token, String ip, String userAgent) {
        log.info("recordLoginSuccess called: userId={}", userId);
        if (userId == null || token == null || token.isBlank()) {
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        repository.insertSession(sessionId, userId, token, ip, userAgent);
        repository.insertLoginHistory(userId, "SUCCESS", ip, userAgent);
    }

    public void recordLoginFailure(Long userId, String ip, String userAgent) {
        log.info("recordLoginFailure called: userId={}", userId);
        if (userId == null) {
            return;
        }
        repository.insertLoginHistory(userId, "FAILED", ip, userAgent);
    }

    public List<LoginHistoryItem> getLoginHistory(Long userId, int limit) {
        log.info("getLoginHistory called: userId={}, limit={}", userId, limit);
        int bounded = Math.max(1, Math.min(limit, MAX_LIMIT));
        return repository.findLoginHistory(userId, bounded);
    }

    public List<SessionItem> getSessions(Long userId, String currentToken) {
        log.info("getSessions called: userId={}", userId);
        List<SessionItem> list = repository.findSessions(userId);
        String currentSessionId = repository.findSessionIdByTokenAndUserId(currentToken, userId).orElse("");
        for (SessionItem item : list) {
            item.setCurrent(item.getSessionId().equals(currentSessionId));
        }
        return list;
    }

    public boolean logoutCurrent(Long userId, String token) {
        log.info("logoutCurrent called: userId={}", userId);
        return repository.deleteSessionByToken(userId, token) > 0;
    }

    public boolean logoutBySessionId(Long userId, String sessionId) {
        log.info("logoutBySessionId called: userId={}, sessionId={}", userId, sessionId);
        return repository.deleteSessionBySessionId(userId, sessionId) > 0;
    }

    public Optional<Long> getUserIdByToken(String token) {
        log.info("getUserIdByToken called");
        return repository.findUserIdByToken(token);
    }
}
