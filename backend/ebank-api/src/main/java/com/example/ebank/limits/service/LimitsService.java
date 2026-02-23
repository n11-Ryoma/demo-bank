package com.example.ebank.limits.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.limits.dto.LimitsResponse;
import com.example.ebank.limits.dto.LimitsUpdateRequest;
import com.example.ebank.limits.repository.jdbc.LimitsRepositoryJdbc;

@Service
public class LimitsService {

    private static final Logger log = LogManager.getLogger(LimitsService.class);
    private static final long DEFAULT_TRANSFER_LIMIT = 500_000L;
    private static final long DEFAULT_ATM_LIMIT = 100_000L;

    private final LimitsRepositoryJdbc repository;

    public LimitsService(LimitsRepositoryJdbc repository) {
        this.repository = repository;
    }

    public LimitsResponse get(Long userId) {
        log.info("get called: userId={}", userId);
        return repository.findByUserId(userId)
                .orElseGet(() -> repository.upsert(userId, DEFAULT_TRANSFER_LIMIT, DEFAULT_ATM_LIMIT));
    }

    public LimitsResponse update(Long userId, LimitsUpdateRequest request) {
        log.info("update called: userId={}, transferLimitYen={}, atmWithdrawLimitYen={}",
                userId,
                request == null ? null : request.getTransferLimitYen(),
                request == null ? null : request.getAtmWithdrawLimitYen());
        if (request.getTransferLimitYen() == null || request.getAtmWithdrawLimitYen() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transferLimitYen and atmWithdrawLimitYen are required");
        }
        if (request.getTransferLimitYen() < 0 || request.getAtmWithdrawLimitYen() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limits must be >= 0");
        }
        return repository.upsert(userId, request.getTransferLimitYen(), request.getAtmWithdrawLimitYen());
    }
}
