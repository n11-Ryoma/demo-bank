package com.example.ebank.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ebank.notifications.dto.NotificationItem;
import com.example.ebank.notifications.repository.jdbc.NotificationRepositoryJdbc;

@Service
public class NotificationService {

    private final NotificationRepositoryJdbc repository;

    public NotificationService(NotificationRepositoryJdbc repository) {
        this.repository = repository;
    }

    public List<NotificationItem> list(Long userId, int limit, boolean unreadOnly) {
        int bounded = Math.max(1, Math.min(limit, 100));
        return repository.findByUserId(userId, bounded, unreadOnly);
    }
}
