package com.example.ebank.address.dto;

public class AddressChangeResponse {
    private String status;

    public AddressChangeResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

