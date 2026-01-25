package com.example.ebank.publicnotices.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.publicnotices.dto.PublicNoticeDetailResponse;
import com.example.ebank.publicnotices.dto.PublicNoticeListResponse;
import com.example.ebank.publicnotices.service.PublicNoticeService;

@RestController
@RequestMapping("/api/public/notices")
public class PublicNoticeController {

    private final PublicNoticeService service;

    public PublicNoticeController(PublicNoticeService service) {
        this.service = service;
    }

    @GetMapping
    public PublicNoticeListResponse list(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.list(Optional.ofNullable(category), safeLimit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicNoticeDetailResponse> detail(@PathVariable long id) {
        try {
            return ResponseEntity.ok(service.detail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
