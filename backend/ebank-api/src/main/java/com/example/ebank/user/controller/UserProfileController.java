package com.example.ebank.user.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.user.entity.UserProfile;
import com.example.ebank.user.service.UserProfileService;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserProfileService service;
    private final JwtUtil jwtUtil;
    private final AuditLogger audit;

    public UserProfileController(UserProfileService service, JwtUtil jwtUtil, AuditLogger audit) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.audit = audit;
    }

    @GetMapping
    public UserProfile getMyProfile(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String actor = "anonymous";
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String username = jwtUtil.extractUsername(token);
            long userId = jwtUtil.extractUserId(token);
            actor = (username == null || username.isBlank()) ? String.valueOf(userId) : username;
            UserProfile profile = service.getProfile(userId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "USER_PROFILE_GET",
                    actor,
                    null,
                    null,
                    new HttpMeta("/api/user/profile", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return profile;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "USER_PROFILE_GET",
                    actor,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/user/profile", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}

