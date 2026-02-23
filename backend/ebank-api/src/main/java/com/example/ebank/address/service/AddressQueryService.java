package com.example.ebank.address.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.example.ebank.address.dto.CurrentAddressResponse;
import com.example.ebank.address.repository.jdbc.AddressRepositoryJdbc;

@Service
public class AddressQueryService {

    private static final Logger log = LogManager.getLogger(AddressQueryService.class);
    private final AddressRepositoryJdbc addressRepositoryJdbc;

    public AddressQueryService(AddressRepositoryJdbc addressRepositoryJdbc) {
        this.addressRepositoryJdbc = addressRepositoryJdbc;
    }

    public CurrentAddressResponse getCurrentAddress(Long userId) {
        log.info("getCurrentAddress called: userId={}", userId);
        return addressRepositoryJdbc.findLatestAddressByUserId(userId);
    }
}
