// src/main/java/com/example/ebank/auth/dto/RegisterResponse.java
package com.example.ebank.auth.dto;

public class RegisterResponse {
    private String username;
    private String accountNumber;
    private String message;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
