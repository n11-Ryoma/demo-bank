package com.example.ebank.publicinfo.dto;

import java.util.List;

public class FaqItem {
    private String id;
    private String category;
    private String question;
    private String answer;
    private List<String> tags;

    public FaqItem(String id, String category, String question, String answer, List<String> tags) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.tags = tags;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
