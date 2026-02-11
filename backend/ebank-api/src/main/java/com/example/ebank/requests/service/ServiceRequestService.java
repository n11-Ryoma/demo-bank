package com.example.ebank.requests.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.requests.dto.RequestDetailResponse;
import com.example.ebank.requests.dto.RequestItem;
import com.example.ebank.requests.repository.jdbc.ServiceRequestRepositoryJdbc;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepositoryJdbc repository;

    public ServiceRequestService(ServiceRequestRepositoryJdbc repository) {
        this.repository = repository;
    }

    public List<RequestItem> list(Long userId, int limit) {
        int bounded = Math.max(1, Math.min(limit, 100));
        return repository.findAllByUserId(userId, bounded);
    }

    public RequestDetailResponse get(Long userId, Long requestId) {
        return repository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    public Long create(Long userId, String requestType, String status, String title, String detail) {
        return repository.insert(userId, requestType, status, title, detail);
    }
}
