package com.example.ebank.security.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.observability.SecurityEventLogger;
import com.example.ebank.security.dto.LoginHistoryItem;
import com.example.ebank.security.dto.LogoutResponse;
import com.example.ebank.security.dto.SessionItem;
import com.example.ebank.security.service.SecuritySessionService;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final AuthTokenParser authTokenParser;
    private final SecuritySessionService securitySessionService;
    private final AuditLogger audit;
    private final SecurityEventLogger sec;

    public SecurityController(AuthTokenParser authTokenParser,
                              SecuritySessionService securitySessionService,
                              AuditLogger audit,
                              SecurityEventLogger sec) {
        this.authTokenParser = authTokenParser;
        this.securitySessionService = securitySessionService;
        this.audit = audit;
        this.sec = sec;
    }

    @GetMapping("/login-history")
    public List<LoginHistoryItem> getLoginHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            List<LoginHistoryItem> res = securitySessionService.getLoginHistory(userId, limit);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "SECURITY_LOGIN_HISTORY_GET",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/security/login-history", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit, "count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "SECURITY_LOGIN_HISTORY_GET",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/security/login-history", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit)
            );
            throw e;
        }
    }

    @PostMapping("/logout")
    public LogoutResponse logoutCurrent(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String token = authTokenParser.extractToken(authHeader);
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        boolean removed = securitySessionService.logoutCurrent(userId, token);
        long latencyMs = (System.nanoTime() - start) / 1_000_000;
        if (!removed) {
            sec.emit(
                    "SECURITY_LOGOUT_SESSION_NOT_FOUND",
                    "LOW",
                    username,
                    ip,
                    Map.of("path", "/api/security/logout")
            );
        }
        audit.success(
                "SECURITY_LOGOUT",
                username,
                null,
                null,
                new HttpMeta("/api/security/logout", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                Map.of("removed", removed)
        );
        return new LogoutResponse("logged out");
    }

    @GetMapping("/sessions")
    public List<SessionItem> getSessions(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String token = authTokenParser.extractToken(authHeader);
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            List<SessionItem> res = securitySessionService.getSessions(userId, token);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "SECURITY_SESSIONS_GET",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/security/sessions", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "SECURITY_SESSIONS_GET",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/security/sessions", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        authTokenParser.extractToken(authHeader);
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        boolean removed = securitySessionService.logoutBySessionId(userId, sessionId);
        long latencyMs = (System.nanoTime() - start) / 1_000_000;
        if (!removed) {
            sec.emit(
                    "SECURITY_DELETE_SESSION_NOT_FOUND",
                    "MEDIUM",
                    username,
                    ip,
                    Map.of("sessionId", sessionId)
            );
            audit.fail(
                    "SECURITY_SESSION_DELETE",
                    username,
                    sessionId,
                    null,
                    "NOT_FOUND",
                    new HttpMeta("/api/security/sessions/" + sessionId, "DELETE", 404, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
        audit.success(
                "SECURITY_SESSION_DELETE",
                username,
                sessionId,
                null,
                new HttpMeta("/api/security/sessions/" + sessionId, "DELETE", 204, ip, ua == null ? "" : ua, latencyMs),
                Map.of()
        );
    }
}
