package com.example.ebank.auth.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.auth.jwt.JwtUtil;

@Component
public class AuthTokenParser {

    private static final Logger log = LogManager.getLogger(AuthTokenParser.class);
    private final JwtUtil jwtUtil;

    public AuthTokenParser(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("extractToken failed: missing or invalid Authorization header");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is required");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtUtil.validateToken(token)) {
            log.warn("extractToken failed: token validation error");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return token;
    }

    public String extractUsername(String authHeader) {
        String token = extractToken(authHeader);
        return jwtUtil.extractUsername(token);
    }

    public Long extractUserId(String authHeader) {
        String token = extractToken(authHeader);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return userId;
    }
}
