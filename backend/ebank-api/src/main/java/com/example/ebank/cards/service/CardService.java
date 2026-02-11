package com.example.ebank.cards.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.cards.dto.CardActionResponse;
import com.example.ebank.cards.dto.CardItem;
import com.example.ebank.cards.repository.jdbc.CardRepositoryJdbc;
import com.example.ebank.requests.service.ServiceRequestService;

@Service
public class CardService {

    private final CardRepositoryJdbc cardRepository;
    private final ServiceRequestService requestService;

    public CardService(CardRepositoryJdbc cardRepository, ServiceRequestService requestService) {
        this.cardRepository = cardRepository;
        this.requestService = requestService;
    }

    public List<CardItem> list(Long userId) {
        return cardRepository.findAllByUserId(userId);
    }

    public CardActionResponse lock(Long userId, Long cardId) {
        ensureCardExists(userId, cardId);
        cardRepository.updateLockState(cardId, userId, true, "LOCKED");
        return new CardActionResponse("card locked", null);
    }

    public CardActionResponse unlock(Long userId, Long cardId) {
        ensureCardExists(userId, cardId);
        cardRepository.updateLockState(cardId, userId, false, "ACTIVE");
        return new CardActionResponse("card unlocked", null);
    }

    public CardActionResponse reissue(Long userId, Long cardId) {
        ensureCardExists(userId, cardId);
        cardRepository.updateStatus(cardId, userId, "REISSUE_REQUESTED");
        Long requestId = requestService.create(
                userId,
                "CARD_REISSUE",
                "RECEIVED",
                "Card reissue request",
                "Reissue requested for cardId=" + cardId
        );
        return new CardActionResponse("reissue requested", requestId);
    }

    private void ensureCardExists(Long userId, Long cardId) {
        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
    }
}
