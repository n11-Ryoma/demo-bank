package com.example.ebank.address.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.address.dto.AddressChangeCommitRequest;
import com.example.ebank.address.dto.AddressChangeResponse;
import com.example.ebank.address.dto.CurrentAddressResponse;
import com.example.ebank.address.service.AddressChangeService;
import com.example.ebank.address.service.AddressQueryService;
import com.example.ebank.auth.jwt.JwtUtil;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/address-change")
public class AddressChangeController {

    private final AddressChangeService service;
    private final AddressQueryService queryService;
    private final JwtUtil jwtUtil;
    private final AuditLogger audit;

    public AddressChangeController(AddressChangeService service,
                                   AddressQueryService queryService,
                                   JwtUtil jwtUtil,
                                   AuditLogger audit) {
        this.service = service;
        this.queryService = queryService;
        this.jwtUtil = jwtUtil;
        this.audit = audit;
    }
    
    @GetMapping("/current")
    public ResponseEntity<CurrentAddressResponse> getCurrentAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpReq) {

        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CURRENT",
                    "anonymous",
                    null,
                    null,
                    "UNAUTHORIZED",
                    new HttpMeta("/api/address-change/current", "GET", 401, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CURRENT",
                    "anonymous",
                    null,
                    null,
                    "UNAUTHORIZED",
                    new HttpMeta("/api/address-change/current", "GET", 401, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CurrentAddressResponse current = queryService.getCurrentAddress(userId);
        if (current == null) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CURRENT",
                    String.valueOf(userId),
                    null,
                    null,
                    "NOT_FOUND",
                    new HttpMeta("/api/address-change/current", "GET", 404, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long latencyMs = (System.nanoTime() - start) / 1_000_000;
        audit.success(
                "ADDRESS_CURRENT",
                String.valueOf(userId),
                null,
                null,
                new HttpMeta("/api/address-change/current", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                Map.of()
        );
        return ResponseEntity.ok(current);
    }
    
    @PostMapping("/commit")
    public ResponseEntity<AddressChangeResponse> commit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddressChangeCommitRequest req,
            HttpServletRequest httpReq) {

        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");

        // Authorization check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CHANGE_COMMIT",
                    "anonymous",
                    null,
                    null,
                    "UNAUTHORIZED",
                    new HttpMeta("/api/address-change/commit", "POST", 401, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CHANGE_COMMIT",
                    "anonymous",
                    null,
                    null,
                    "UNAUTHORIZED",
                    new HttpMeta("/api/address-change/commit", "POST", 401, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AddressChangeResponse res;
        try {
            res = service.commit(userId, req);
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "ADDRESS_CHANGE_COMMIT",
                    String.valueOf(userId),
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/address-change/commit", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }

        long latencyMs = (System.nanoTime() - start) / 1_000_000;
        audit.success(
                "ADDRESS_CHANGE_COMMIT",
                String.valueOf(userId),
                null,
                null,
                new HttpMeta("/api/address-change/commit", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                Map.of()
        );
        return ResponseEntity.ok(res);
    }
}
