package com.example.ebank.cards.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.example.ebank.observability.AuditLogger;
import com.example.ebank.observability.HttpMeta;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final AuthTokenParser authTokenParser;
    private final CardService cardService;
    private final AuditLogger audit;

    public CardController(AuthTokenParser authTokenParser, CardService cardService, AuditLogger audit) {
        this.authTokenParser = authTokenParser;
        this.cardService = cardService;
        this.audit = audit;
    }

    @GetMapping
    public List<CardItem> list(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            List<CardItem> res = cardService.list(userId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "CARD_LIST",
                    username,
                    null,
                    null,
                    new HttpMeta("/api/cards", "GET", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("count", res.size())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "CARD_LIST",
                    username,
                    null,
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/cards", "GET", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping("/{cardId}/lock")
    public CardActionResponse lock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            CardActionResponse res = cardService.lock(userId, cardId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "CARD_LOCK",
                    username,
                    String.valueOf(cardId),
                    null,
                    new HttpMeta("/api/cards/" + cardId + "/lock", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "CARD_LOCK",
                    username,
                    String.valueOf(cardId),
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/cards/" + cardId + "/lock", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping("/{cardId}/unlock")
    public CardActionResponse unlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            CardActionResponse res = cardService.unlock(userId, cardId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "CARD_UNLOCK",
                    username,
                    String.valueOf(cardId),
                    null,
                    new HttpMeta("/api/cards/" + cardId + "/unlock", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "CARD_UNLOCK",
                    username,
                    String.valueOf(cardId),
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/cards/" + cardId + "/unlock", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }

    @PostMapping("/{cardId}/reissue")
    public CardActionResponse reissue(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cardId,
            HttpServletRequest httpReq) {
        long start = System.nanoTime();
        String ip = com.example.ebank.observability.ClientIpResolver.resolve(httpReq);
        String ua = httpReq.getHeader("User-Agent");
        String username = authTokenParser.extractUsername(authHeader);
        Long userId = authTokenParser.extractUserId(authHeader);
        try {
            CardActionResponse res = cardService.reissue(userId, cardId);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.success(
                    "CARD_REISSUE",
                    username,
                    String.valueOf(cardId),
                    null,
                    new HttpMeta("/api/cards/" + cardId + "/reissue", "POST", 200, ip, ua == null ? "" : ua, latencyMs),
                    Map.of("requestId", res.getRequestId() == null ? "" : res.getRequestId())
            );
            return res;
        } catch (RuntimeException e) {
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            audit.fail(
                    "CARD_REISSUE",
                    username,
                    String.valueOf(cardId),
                    null,
                    e.getClass().getSimpleName(),
                    new HttpMeta("/api/cards/" + cardId + "/reissue", "POST", 500, ip, ua == null ? "" : ua, latencyMs),
                    Map.of()
            );
            throw e;
        }
    }
}

