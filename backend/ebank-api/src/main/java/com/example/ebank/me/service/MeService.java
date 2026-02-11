package com.example.ebank.me.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.me.dto.MeResponse;
import com.example.ebank.me.repository.jdbc.MeRepositoryJdbc;

@Service
public class MeService {

    private final MeRepositoryJdbc meRepositoryJdbc;

    public MeService(MeRepositoryJdbc meRepositoryJdbc) {
        this.meRepositoryJdbc = meRepositoryJdbc;
    }

    public MeResponse getByUsername(String username) {
        MeResponse response = meRepositoryJdbc.findByUsername(username);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return response;
    }
}
