package com.example.ebank.security.dto;

import java.time.OffsetDateTime;

public class SessionItem {

    private String sessionId;
    private OffsetDateTime loginAt;
    private String ip;
    private String userAgent;
    private boolean current;

    public SessionItem() {
    }

    public SessionItem(String sessionId, OffsetDateTime loginAt, String ip, String userAgent, boolean current) {
        this.sessionId = sessionId;
        this.loginAt = loginAt;
        this.ip = ip;
        this.userAgent = userAgent;
        this.current = current;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public OffsetDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(OffsetDateTime loginAt) {
        this.loginAt = loginAt;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
