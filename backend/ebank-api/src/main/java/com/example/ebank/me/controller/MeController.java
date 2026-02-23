package com.example.ebank.me.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.me.dto.MeResponse;
import com.example.ebank.me.service.MeService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
public class MeController {

    private final AuthTokenParser authTokenParser;
    private final MeService meService;
    private final AuditLogger audit;

    public MeController(AuthTokenParser authTokenParser, MeService meService, AuditLogger audit) {
        this.authTokenParser = authTokenParser;
        this.meService = meService;
        this.audit = audit;
    }

    @GetMapping("/api/me")
    public MeResponse getMe(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String actor = "anonymous";
        try {
            String username = authTokenParser.extractUsername(authHeader);
            actor = username;
            MeResponse res = meService.getByUsername(username);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ME_GET",
                    actor,
                    null,
                    null,
                    new HttpMeta("/api/me", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ME_GET",
                    actor,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/me", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}
