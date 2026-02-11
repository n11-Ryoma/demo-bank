package com.example.ebank.cards.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.auth.security.AuthTokenParser;
import com.example.ebank.cards.dto.CardActionResponse;
import com.example.ebank.cards.dto.CardItem;
import com.example.ebank.cards.service.CardService;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final AuthTokenParser authTokenParser;
    private final CardService cardService;

    public CardController(AuthTokenParser authTokenParser, CardService cardService) {
        this.authTokenParser = authTokenParser;
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardItem> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return cardService.list(userId);
    }

    @PostMapping("/{cardId}/lock")
    public CardActionResponse lock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return cardService.lock(userId, cardId);
    }

    @PostMapping("/{cardId}/unlock")
    public CardActionResponse unlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return cardService.unlock(userId, cardId);
    }

    @PostMapping("/{cardId}/reissue")
    public CardActionResponse reissue(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId) {
        Long userId = authTokenParser.extractUserId(authHeader);
        return cardService.reissue(userId, cardId);
    }
}
