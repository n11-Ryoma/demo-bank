package com.example.ebank.beneficiaries.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.beneficiaries.dto.BeneficiaryRequest;
import com.example.ebank.beneficiaries.dto.BeneficiaryResponse;
import com.example.ebank.beneficiaries.entity.Beneficiary;
import com.example.ebank.beneficiaries.repository.BeneficiaryRepository;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public List<BeneficiaryResponse> list(Long userId) {
        return beneficiaryRepository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BeneficiaryResponse create(Long userId, BeneficiaryRequest request) {
        if (isBlank(request.getBankName()) || isBlank(request.getAccountNumber()) || isBlank(request.getAccountHolderName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bankName, accountNumber and accountHolderName are required");
        }

        Beneficiary b = new Beneficiary();
        b.setUserId(userId);
        b.setBankName(request.getBankName());
        b.setBranchName(request.getBranchName());
        b.setAccountType(defaultIfBlank(request.getAccountType(), "ORDINARY"));
        b.setAccountNumber(request.getAccountNumber());
        b.setAccountHolderName(request.getAccountHolderName());
        b.setNickname(request.getNickname());

        return toResponse(beneficiaryRepository.save(b));
    }

    public void delete(Long userId, Long id) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
        beneficiaryRepository.deleteByIdAndUserId(beneficiary.getId(), userId);
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        BeneficiaryResponse response = new BeneficiaryResponse();
        response.setId(b.getId());
        response.setBankName(b.getBankName());
        response.setBranchName(b.getBranchName());
        response.setAccountType(b.getAccountType());
        response.setAccountNumberMasked(maskAccountNumber(b.getAccountNumber()));
        response.setAccountHolderName(b.getAccountHolderName());
        response.setNickname(b.getNickname());
        response.setCreatedAt(b.getCreatedAt());
        return response;
    }

    private String maskAccountNumber(String raw) {
        if (raw == null || raw.length() <= 4) {
            return "****";
        }
        return "****" + raw.substring(raw.length() - 4);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }
}
