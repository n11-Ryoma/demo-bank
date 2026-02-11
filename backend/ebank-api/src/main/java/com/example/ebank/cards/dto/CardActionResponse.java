package com.example.ebank.cards.dto;

public class CardActionResponse {

    private String message;
    private Long requestId;

    public CardActionResponse() {
    }

    public CardActionResponse(String message, Long requestId) {
        this.message = message;
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
