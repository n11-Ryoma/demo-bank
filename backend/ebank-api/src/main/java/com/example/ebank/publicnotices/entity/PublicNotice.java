package com.example.ebank.publicnotices.entity;

import java.time.OffsetDateTime;

public class PublicNotice {
    private long id;
    private String category;
    private String title;
    private String summary;
    private String bodyMarkdown;
    private String bodyHtml;
    private String severity;
    private String statusLabel;
    private boolean isPublished;
    private OffsetDateTime publishFrom;
    private OffsetDateTime publishUntil;
    private String renderProfile;
    private String renderData;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getBodyMarkdown() { return bodyMarkdown; }
    public void setBodyMarkdown(String bodyMarkdown) { this.bodyMarkdown = bodyMarkdown; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public OffsetDateTime getPublishFrom() { return publishFrom; }
    public void setPublishFrom(OffsetDateTime publishFrom) { this.publishFrom = publishFrom; }

    public OffsetDateTime getPublishUntil() { return publishUntil; }
    public void setPublishUntil(OffsetDateTime publishUntil) { this.publishUntil = publishUntil; }

    public String getRenderProfile() { return renderProfile; }
    public void setRenderProfile(String renderProfile) { this.renderProfile = renderProfile; }

    public String getRenderData() { return renderData; }
    public void setRenderData(String renderData) { this.renderData = renderData; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
