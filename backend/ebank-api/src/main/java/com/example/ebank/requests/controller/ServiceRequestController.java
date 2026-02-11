package com.example.ebank.requests.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.requests.dto.RequestDetailResponse;
import com.example.ebank.requests.dto.RequestItem;
import com.example.ebank.requests.service.ServiceRequestService;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final AuthTokenParser authTokenParser;
    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(AuthTokenParser authTokenParser, ServiceRequestService serviceRequestService) {
        this.authTokenParser = authTokenParser;
        this.serviceRequestService = serviceRequestService;
    }

    @GetMapping
    public List<RequestItem> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return serviceRequestService.list(userId, limit);
    }

    @GetMapping("/{requestId}")
    public RequestDetailResponse get(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return serviceRequestService.get(userId, requestId);
    }
}
