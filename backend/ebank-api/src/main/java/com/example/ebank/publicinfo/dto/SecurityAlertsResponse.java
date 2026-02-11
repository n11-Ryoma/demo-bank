package com.example.ebank.publicinfo.dto;

import java.util.List;

public class SecurityAlertsResponse {
    private String generatedAt;
    private List<SecurityAlertItem> items;

    public SecurityAlertsResponse(String generatedAt, List<SecurityAlertItem> items) {
        this.generatedAt = generatedAt;
        this.items = items;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<SecurityAlertItem> getItems() {
        return items;
    }

    public void setItems(List<SecurityAlertItem> items) {
        this.items = items;
    }
}
