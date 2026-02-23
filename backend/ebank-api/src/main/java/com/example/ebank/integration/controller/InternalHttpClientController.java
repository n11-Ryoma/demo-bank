package com.example.ebank.integration.controller;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;
import com.example.ebank.observability.SecurityEventLogger;

@RestController
@RequestMapping("/api/integration")
public class InternalHttpClientController {

    private static final Logger log = LogManager.getLogger(InternalHttpClientController.class.getName());

    private final AuditLogger audit;
    private final SecurityEventLogger sec;

    public InternalHttpClientController(AuditLogger audit, SecurityEventLogger sec) {
        this.audit = audit;
        this.sec = sec;
    }

    @GetMapping("/fetch")
    public String fetchUrl(
            @RequestParam String url,
            @RequestHeader(value = "X-Api-Version", required = false) String apiVersion,
            HttpServletRequest httpReq) {

        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String host = extractHost(url);
        String scheme = extractScheme(url);

        if (apiVersion != null) {
            log.info("X-Api-Version: {}", apiVersion);
        }
        log.info("request to scheme={} host={}", scheme, host);

        if (isSuspiciousTarget(url, host, scheme)) {
            sec.emit(
                    "INTEGRATION_FETCH_SSRF_PATTERN",
                    "HIGH",
                    "anonymous",
                    ip,
                    Map.of("scheme", scheme, "host", host, "path", "/api/integration/fetch")
            );
        }

        try {
            String res = new RestTemplate().getForObject(url, String.class);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "INTEGRATION_FETCH",
                    "anonymous",
                    host,
                    null,
                    new HttpMeta("/api/integration/fetch", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("scheme", scheme)
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            sec.emit(
                    "INTEGRATION_FETCH_FAILED",
                    "MEDIUM",
                    "anonymous",
                    ip,
                    Map.of("scheme", scheme, "host", host, "reason", e.getClass().getSimpleName())
            );
            audit.fail(
                    "INTEGRATION_FETCH",
                    "anonymous",
                    host,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/integration/fetch", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("scheme", scheme)
            );
            throw e;
        }
    }

    private String extractHost(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            return uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private String extractScheme(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            return uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private boolean isSuspiciousTarget(String rawUrl, String host, String scheme) {
        if ("file".equals(scheme) || "gopher".equals(scheme) || "ftp".equals(scheme)) {
            return true;
        }
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return true;
        }
        if (host.isBlank()) {
            return true;
        }
        if ("localhost".equals(host) || host.startsWith("127.") || host.startsWith("0.") || host.equals("::1")) {
            return true;
        }
        if (host.startsWith("10.") || host.startsWith("192.168.") || host.startsWith("169.254.")) {
            return true;
        }
        if (host.startsWith("172.")) {
            String[] parts = host.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    return rawUrl.contains("@");
                }
            }
        }
        return rawUrl.contains("@");
    }
}
