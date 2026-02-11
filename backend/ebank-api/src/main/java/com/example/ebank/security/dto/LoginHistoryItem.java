package com.example.ebank.security.dto;

import java.time.OffsetDateTime;

public class LoginHistoryItem {

    private OffsetDateTime occurredAt;
    private String ip;
    private String userAgent;
    private String result;

    public LoginHistoryItem() {
    }

    public LoginHistoryItem(OffsetDateTime occurredAt, String ip, String userAgent, String result) {
        this.occurredAt = occurredAt;
        this.ip = ip;
        this.userAgent = userAgent;
        this.result = result;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
