package com.example.ebank.notifications.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.example.ebank.notifications.dto.NotificationItem;
import com.example.ebank.notifications.repository.jdbc.NotificationRepositoryJdbc;

@Service
public class NotificationService {

    private static final Logger log = LogManager.getLogger(NotificationService.class);
    private final NotificationRepositoryJdbc repository;

    public NotificationService(NotificationRepositoryJdbc repository) {
        this.repository = repository;
    }

    public List<NotificationItem> list(Long userId, int limit, boolean unreadOnly) {
        log.info("list called: userId={}, limit={}, unreadOnly={}", userId, limit, unreadOnly);
        int bounded = Math.max(1, Math.min(limit, 100));
        return repository.findByUserId(userId, bounded, unreadOnly);
    }
}
