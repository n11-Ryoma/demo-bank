package com.example.ebank.requests.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.requests.dto.RequestDetailResponse;
import com.example.ebank.requests.dto.RequestItem;
import com.example.ebank.requests.service.ServiceRequestService;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final AuthTokenParser authTokenParser;
    private final ServiceRequestService serviceRequestService;
    private final AuditLogger audit;

    public ServiceRequestController(AuthTokenParser authTokenParser,
                                    ServiceRequestService serviceRequestService,
                                    AuditLogger audit) {
        this.authTokenParser = authTokenParser;
        this.serviceRequestService = serviceRequestService;
        this.audit = audit;
    }

    @GetMapping
    public List<RequestItem> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String actor = "anonymous";
        try {
            String username = authTokenParser.extractUsername(authHeader);
            Long userId = authTokenParser.extractUserId(authHeader);
            actor = username;
            List<RequestItem> res = serviceRequestService.list(userId, limit);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "SERVICE_REQUEST_LIST",
                    actor,
                    null,
                    null,
                    new HttpMeta("/api/requests", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit, "count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "SERVICE_REQUEST_LIST",
                    actor,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/requests", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit)
            );
            throw e;
        }
    }

    @GetMapping("/{requestId}")
    public RequestDetailResponse get(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String actor = "anonymous";
        try {
            String username = authTokenParser.extractUsername(authHeader);
            Long userId = authTokenParser.extractUserId(authHeader);
            actor = username;
            RequestDetailResponse res = serviceRequestService.get(userId, requestId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "SERVICE_REQUEST_GET",
                    actor,
                    String.valueOf(requestId),
                    null,
                    new HttpMeta("/api/requests/" + requestId, "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "SERVICE_REQUEST_GET",
                    actor,
                    String.valueOf(requestId),
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/requests/" + requestId, "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}

