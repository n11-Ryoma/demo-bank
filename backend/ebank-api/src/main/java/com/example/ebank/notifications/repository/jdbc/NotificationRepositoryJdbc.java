package com.example.ebank.notifications.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.notifications.dto.NotificationItem;

@Repository
public class NotificationRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public NotificationRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<NotificationItem> findByUserId(Long userId, int limit, boolean unreadOnly) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, title, message, severity, category, is_read, created_at
            FROM notifications
            WHERE user_id = :userId
            """);
        if (unreadOnly) {
            sql.append(" AND is_read = false ");
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit ");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit);
        return jdbc.query(sql.toString(), params, this::mapRow);
    }

    private NotificationItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotificationItem item = new NotificationItem();
        item.setId(rs.getLong("id"));
        item.setTitle(rs.getString("title"));
        item.setMessage(rs.getString("message"));
        item.setSeverity(rs.getString("severity"));
        item.setCategory(rs.getString("category"));
        item.setRead(rs.getBoolean("is_read"));
        item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return item;
    }
}
