package com.example.ebank.publicinfo.dto;

public class SecurityAlertItem {
    private String id;
    private String title;
    private String tag;
    private String riskLevel;
    private int recentCount;
    private String tip;
    private String updatedAt;

    public SecurityAlertItem(String id, String title, String tag, String riskLevel,
                             int recentCount, String tip, String updatedAt) {
        this.id = id;
        this.title = title;
        this.tag = tag;
        this.riskLevel = riskLevel;
        this.recentCount = recentCount;
        this.tip = tip;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getRecentCount() {
        return recentCount;
    }

    public void setRecentCount(int recentCount) {
        this.recentCount = recentCount;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
