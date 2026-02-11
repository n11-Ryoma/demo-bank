// src/main/java/com/example/ebank/auth/dto/RegisterRequest.java
package com.example.ebank.auth.dto;

import com.example.ebank.user.dto.UserProfileRequest;

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private UserProfileRequest profile;

    public UserProfileRequest getProfile() {
        return profile;
    }
    public void setProfile(UserProfileRequest profile) {
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
