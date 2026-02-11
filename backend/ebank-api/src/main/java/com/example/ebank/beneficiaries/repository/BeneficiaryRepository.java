package com.example.ebank.beneficiaries.repository;

import java.util.List;
import java.util.Optional;

import com.example.ebank.beneficiaries.entity.Beneficiary;

public interface BeneficiaryRepository {

    List<Beneficiary> findAllByUserId(Long userId);

    Beneficiary save(Beneficiary beneficiary);

    Optional<Beneficiary> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
