package com.example.ebank.onboarding.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.onboarding.dto.OpenAccountRequest;
import com.example.ebank.onboarding.dto.OpenAccountResponse;
import com.example.ebank.onboarding.service.OnboardingService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService service;
    private final AuditLogger audit;

    public OnboardingController(OnboardingService service, AuditLogger audit) {
        this.service = service;
        this.audit = audit;
    }

    @PostMapping("/open-account")
    public OpenAccountResponse openAccount(@RequestBody OpenAccountRequest req, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        try {
            OpenAccountResponse res = service.openAccount(req);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "ONBOARDING_OPEN_ACCOUNT",
                    req.getUsername(),
                    res.getAccountNumber(),
                    null,
                    new HttpMeta("/api/onboarding/open-account", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("userId", res.getUserId(), "accountId", res.getAccountId())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ONBOARDING_OPEN_ACCOUNT",
                    req.getUsername(),
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/onboarding/open-account", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}
