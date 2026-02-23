package com.example.ebank.notifications.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.notifications.dto.NotificationItem;
import com.example.ebank.notifications.service.NotificationService;
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final AuthTokenParser authTokenParser;
    private final NotificationService notificationService;
    private final AuditLogger audit;

    public NotificationController(AuthTokenParser authTokenParser, NotificationService notificationService, AuditLogger audit) {
        this.authTokenParser = authTokenParser;
        this.notificationService = notificationService;
        this.audit = audit;
    }

    @GetMapping
    public List<NotificationItem> getNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = httpReq.getRemoteAddr();
        String ua = httpReq.getHeader("User-Agent");
        String actor = "anonymous";
        try {
            String username = authTokenParser.extractUsername(authHeader);
            Long userId = authTokenParser.extractUserId(authHeader);
            actor = username;
            List<NotificationItem> res = notificationService.list(userId, limit, unreadOnly);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "NOTIFICATION_LIST",
                    actor,
                    null,
                    null,
                    new HttpMeta("/api/notifications", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit, "unreadOnly", unreadOnly, "count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "NOTIFICATION_LIST",
                    actor,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/notifications", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("limit", limit, "unreadOnly", unreadOnly)
            );
            throw e;
        }
    }
}
