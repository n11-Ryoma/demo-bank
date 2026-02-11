package com.example.ebank.notifications.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.notifications.dto.NotificationItem;
import com.example.ebank.notifications.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final AuthTokenParser authTokenParser;
    private final NotificationService notificationService;

    public NotificationController(AuthTokenParser authTokenParser, NotificationService notificationService) {
        this.authTokenParser = authTokenParser;
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationItem> getNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return notificationService.list(userId, limit, unreadOnly);
    }
}
