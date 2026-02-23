package com.example.ebank.limits.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.limits.dto.LimitsResponse;
import com.example.ebank.limits.dto.LimitsUpdateRequest;
import com.example.ebank.limits.service.LimitsService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.observability.SecurityEventLogger;

@RestController
@RequestMapping("/api/limits")
public class LimitsController {

    private final AuthTokenParser authTokenParser;
    private final LimitsService limitsService;
    private final AuditLogger audit;
    private final SecurityEventLogger sec;

    public LimitsController(AuthTokenParser authTokenParser,
                            LimitsService limitsService,
                            AuditLogger audit,
                            SecurityEventLogger sec) {
        this.authTokenParser = authTokenParser;
        this.limitsService = limitsService;
        this.audit = audit;
        this.sec = sec;
    }

    @GetMapping
    public LimitsResponse getLimits(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            LimitsResponse res = limitsService.get(userId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "LIMITS_GET",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/limits", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("transferLimitYen", res.getTransferLimitYen(), "atmWithdrawLimitYen", res.getAtmWithdrawLimitYen())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "LIMITS_GET",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/limits", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PutMapping
    public LimitsResponse updateLimits(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LimitsUpdateRequest request,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        LimitsResponse before = limitsService.get(userId);
        try {
            LimitsResponse updated = limitsService.update(userId, request);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;

            if (updated.getTransferLimitYen() == 0L || updated.getAtmWithdrawLimitYen() == 0L) {
                sec.emit(
                        "LIMITS_SET_TO_ZERO",
                        "MEDIUM",
                        username,
                        ip,
                        Map.of("transferLimitYen", updated.getTransferLimitYen(), "atmWithdrawLimitYen", updated.getAtmWithdrawLimitYen())
                );
            } else if (updated.getTransferLimitYen() * 5 < before.getTransferLimitYen()
                    || updated.getAtmWithdrawLimitYen() * 5 < before.getAtmWithdrawLimitYen()) {
                sec.emit(
                        "LIMITS_SHARP_REDUCTION",
                        "LOW",
                        username,
                        ip,
                        Map.of(
                                "beforeTransferLimitYen", before.getTransferLimitYen(),
                                "afterTransferLimitYen", updated.getTransferLimitYen(),
                                "beforeAtmWithdrawLimitYen", before.getAtmWithdrawLimitYen(),
                                "afterAtmWithdrawLimitYen", updated.getAtmWithdrawLimitYen()
                        )
                );
            }

            audit.success(
                    "LIMITS_UPDATE",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/limits", "PUT", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of(
                            "beforeTransferLimitYen", before.getTransferLimitYen(),
                            "afterTransferLimitYen", updated.getTransferLimitYen(),
                            "beforeAtmWithdrawLimitYen", before.getAtmWithdrawLimitYen(),
                            "afterAtmWithdrawLimitYen", updated.getAtmWithdrawLimitYen()
                    )
            );
            return updated;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            long requestedTransfer = request.getTransferLimitYen() == null ? -1L : request.getTransferLimitYen();
            long requestedAtmWithdraw = request.getAtmWithdrawLimitYen() == null ? -1L : request.getAtmWithdrawLimitYen();
            audit.fail(
                    "LIMITS_UPDATE",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/limits", "PUT", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of(
                            "requestedTransferLimitYen", requestedTransfer,
                            "requestedAtmWithdrawLimitYen", requestedAtmWithdraw
                    )
            );
            throw e;
        }
    }
}

