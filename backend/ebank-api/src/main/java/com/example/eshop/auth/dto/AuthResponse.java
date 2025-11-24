package com.example.eshop.auth.dto;

import java.util.List;

public class AuthResponse {

    private String token;
    private String status;
    private String username;
    private List<String> roles;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String status, String username, List<String> roles, String message) {
        this.token = token;
        this.status = status;
        this.username = username;
        this.roles = roles;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
