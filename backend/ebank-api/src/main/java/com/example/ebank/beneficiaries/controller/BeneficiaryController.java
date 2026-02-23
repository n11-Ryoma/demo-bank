package com.example.ebank.beneficiaries.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.beneficiaries.dto.BeneficiaryRequest;
import com.example.ebank.beneficiaries.dto.BeneficiaryResponse;
import com.example.ebank.beneficiaries.service.BeneficiaryService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final AuthTokenParser authTokenParser;
    private final BeneficiaryService beneficiaryService;
    private final AuditLogger audit;

    public BeneficiaryController(AuthTokenParser authTokenParser, BeneficiaryService beneficiaryService, AuditLogger audit) {
        this.authTokenParser = authTokenParser;
        this.beneficiaryService = beneficiaryService;
        this.audit = audit;
    }

    @GetMapping
    public List<BeneficiaryResponse> list(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        long userId = authTokenParser.extractUserId(authHeader);
        try {
            List<BeneficiaryResponse> res = beneficiaryService.list(userId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "BENEFICIARY_LIST",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/beneficiaries", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "BENEFICIARY_LIST",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/beneficiaries", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryResponse create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BeneficiaryRequest request,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        long userId = authTokenParser.extractUserId(authHeader);
        String accountSuffix = "****";
        if (request.getAccountNumber() != null && request.getAccountNumber().length() > 4) {
            accountSuffix = "****" + request.getAccountNumber().substring(request.getAccountNumber().length() - 4);
        }
        try {
            BeneficiaryResponse res = beneficiaryService.create(userId, request);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "BENEFICIARY_CREATE",
                    username,
                    String.valueOf(res.getId()),
                    null,
                    new HttpMeta("/api/beneficiaries", "POST", 201, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("bankName", res.getBankName(), "accountNumberMasked", accountSuffix)
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "BENEFICIARY_CREATE",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/beneficiaries", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("bankName", request.getBankName() == null ? "" : request.getBankName(), "accountNumberMasked", accountSuffix)
            );
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        long userId = authTokenParser.extractUserId(authHeader);
        try {
            beneficiaryService.delete(userId, id);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "BENEFICIARY_DELETE",
                    username,
                    String.valueOf(id),
                    null,
                    new HttpMeta("/api/beneficiaries/" + id, "DELETE", 204, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "BENEFICIARY_DELETE",
                    username,
                    String.valueOf(id),
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/beneficiaries/" + id, "DELETE", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}

