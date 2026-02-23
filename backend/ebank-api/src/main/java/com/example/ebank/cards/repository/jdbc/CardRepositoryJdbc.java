package com.example.ebank.cards.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.cards.dto.CardItem;

@Repository
public class CardRepositoryJdbc {

    private static final Logger log = LogManager.getLogger(CardRepositoryJdbc.class);
    private final NamedParameterJdbcTemplate jdbc;

    public CardRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CardItem> findAllByUserId(Long userId) {
        log.info("findAllByUserId called: userId={}", userId);
        String sql = """
            SELECT id, card_type, masked_number, status, locked, updated_at
            FROM cards
            WHERE user_id = :userId
            ORDER BY id ASC
            """;
        return jdbc.query(sql, Map.of("userId", userId), this::mapRow);
    }

    public Optional<CardItem> findByIdAndUserId(Long cardId, Long userId) {
        log.info("findByIdAndUserId called: cardId={}, userId={}", cardId, userId);
        String sql = """
            SELECT id, card_type, masked_number, status, locked, updated_at
            FROM cards
            WHERE id = :cardId AND user_id = :userId
            """;
        List<CardItem> list = jdbc.query(sql, Map.of("cardId", cardId, "userId", userId), this::mapRow);
        return list.stream().findFirst();
    }

    public void updateLockState(Long cardId, Long userId, boolean locked, String status) {
        log.info("updateLockState called: cardId={}, userId={}, locked={}, status={}", cardId, userId, locked, status);
        String sql = """
            UPDATE cards
            SET locked = :locked, status = :status, updated_at = now()
            WHERE id = :cardId AND user_id = :userId
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("locked", locked)
                .addValue("status", status)
                .addValue("cardId", cardId)
                .addValue("userId", userId));
    }

    public void updateStatus(Long cardId, Long userId, String status) {
        log.info("updateStatus called: cardId={}, userId={}, status={}", cardId, userId, status);
        String sql = """
            UPDATE cards
            SET status = :status, updated_at = now()
            WHERE id = :cardId AND user_id = :userId
            """;
        jdbc.update(sql, Map.of("status", status, "cardId", cardId, "userId", userId));
    }

    private CardItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardItem item = new CardItem();
        item.setCardId(rs.getLong("id"));
        item.setCardType(rs.getString("card_type"));
        item.setMaskedNumber(rs.getString("masked_number"));
        item.setStatus(rs.getString("status"));
        item.setLocked(rs.getBoolean("locked"));
        item.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return item;
    }
}
