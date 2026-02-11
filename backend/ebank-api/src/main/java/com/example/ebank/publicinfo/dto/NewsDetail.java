package com.example.ebank.publicinfo.dto;

public class NewsDetail {
    private String id;
    private String category;
    private String title;
    private String summary;
    private String body;
    private String publishedAt;
    private String updatedAt;

    public NewsDetail(String id, String category, String title, String summary, String body,
                      String publishedAt, String updatedAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
