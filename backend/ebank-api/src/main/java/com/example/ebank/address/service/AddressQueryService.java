package com.example.ebank.address.service;

import org.springframework.stereotype.Service;

import com.example.ebank.address.dto.CurrentAddressResponse;
import com.example.ebank.address.repository.jdbc.AddressRepositoryJdbc;

@Service
public class AddressQueryService {

    private final AddressRepositoryJdbc addressRepositoryJdbc;

    public AddressQueryService(AddressRepositoryJdbc addressRepositoryJdbc) {
        this.addressRepositoryJdbc = addressRepositoryJdbc;
    }

    public CurrentAddressResponse getCurrentAddress(Long userId) {
        return addressRepositoryJdbc.findLatestAddressByUserId(userId);
    }
}
