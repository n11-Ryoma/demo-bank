package com.example.ebank.security.controller;

import java.util.List;

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
import com.example.ebank.security.dto.LoginHistoryItem;
import com.example.ebank.security.dto.LogoutResponse;
import com.example.ebank.security.dto.SessionItem;
import com.example.ebank.security.service.SecuritySessionService;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final AuthTokenParser authTokenParser;
    private final SecuritySessionService securitySessionService;

    public SecurityController(AuthTokenParser authTokenParser, SecuritySessionService securitySessionService) {
        this.authTokenParser = authTokenParser;
        this.securitySessionService = securitySessionService;
    }

    @GetMapping("/login-history")
    public List<LoginHistoryItem> getLoginHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return securitySessionService.getLoginHistory(userId, limit);
    }

    @PostMapping("/logout")
    public LogoutResponse logoutCurrent(@RequestHeader("Authorization") String authHeader) {
        String token = authTokenParser.extractToken(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        securitySessionService.logoutCurrent(userId, token);
        return new LogoutResponse("logged out");
    }

    @GetMapping("/sessions")
    public List<SessionItem> getSessions(@RequestHeader("Authorization") String authHeader) {
        String token = authTokenParser.extractToken(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        return securitySessionService.getSessions(userId, token);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId) {
        authTokenParser.extractToken(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        boolean removed = securitySessionService.logoutBySessionId(userId, sessionId);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
    }
}
