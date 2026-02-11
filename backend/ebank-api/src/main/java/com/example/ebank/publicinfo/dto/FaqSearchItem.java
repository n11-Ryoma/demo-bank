package com.example.ebank.publicinfo.dto;

import java.util.List;

public class FaqSearchItem {
    private String id;
    private String category;
    private String question;
    private String answer;
    private List<String> highlights;

    public FaqSearchItem(String id, String category, String question, String answer, List<String> highlights) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.highlights = highlights;
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

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }
}
