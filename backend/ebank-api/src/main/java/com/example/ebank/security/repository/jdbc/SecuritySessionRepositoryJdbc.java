package com.example.ebank.security.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.security.dto.LoginHistoryItem;
import com.example.ebank.security.dto.SessionItem;

@Repository
public class SecuritySessionRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public SecuritySessionRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertSession(String sessionId, Long userId, String token, String ip, String userAgent) {
        String sql = """
            INSERT INTO user_sessions
            (session_id, user_id, jwt_token, ip, user_agent)
            VALUES
            (:sessionId, :userId, :token, :ip, :userAgent)
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("sessionId", sessionId)
                .addValue("userId", userId)
                .addValue("token", token)
                .addValue("ip", safe(ip))
                .addValue("userAgent", safe(userAgent)));
    }

    public void insertLoginHistory(Long userId, String result, String ip, String userAgent) {
        String sql = """
            INSERT INTO login_history
            (user_id, result, ip, user_agent)
            VALUES
            (:userId, :result, :ip, :userAgent)
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("result", result)
                .addValue("ip", safe(ip))
                .addValue("userAgent", safe(userAgent)));
    }

    public List<LoginHistoryItem> findLoginHistory(Long userId, int limit) {
        String sql = """
            SELECT occurred_at, ip, user_agent, result
            FROM login_history
            WHERE user_id = :userId
            ORDER BY occurred_at DESC, id DESC
            LIMIT :limit
            """;
        return jdbc.query(sql, Map.of("userId", userId, "limit", limit), this::mapLoginHistory);
    }

    public List<SessionItem> findSessions(Long userId) {
        String sql = """
            SELECT session_id, login_at, ip, user_agent
            FROM user_sessions
            WHERE user_id = :userId
            ORDER BY login_at DESC
            """;
        return jdbc.query(sql, Map.of("userId", userId), this::mapSession);
    }

    public int deleteSessionByToken(Long userId, String token) {
        String sql = "DELETE FROM user_sessions WHERE user_id = :userId AND jwt_token = :token";
        return jdbc.update(sql, Map.of("userId", userId, "token", token));
    }

    public int deleteSessionBySessionId(Long userId, String sessionId) {
        String sql = "DELETE FROM user_sessions WHERE user_id = :userId AND session_id = :sessionId";
        return jdbc.update(sql, Map.of("userId", userId, "sessionId", sessionId));
    }

    public Optional<Long> findUserIdByToken(String token) {
        String sql = "SELECT user_id FROM user_sessions WHERE jwt_token = :token LIMIT 1";
        List<Long> list = jdbc.query(sql, Map.of("token", token), (rs, rowNum) -> rs.getLong("user_id"));
        return list.stream().findFirst();
    }

    public Optional<String> findSessionIdByTokenAndUserId(String token, Long userId) {
        String sql = """
            SELECT session_id
            FROM user_sessions
            WHERE jwt_token = :token AND user_id = :userId
            LIMIT 1
            """;
        List<String> list = jdbc.query(sql, Map.of("token", token, "userId", userId), (rs, rowNum) -> rs.getString("session_id"));
        return list.stream().findFirst();
    }

    private LoginHistoryItem mapLoginHistory(ResultSet rs, int rowNum) throws SQLException {
        return new LoginHistoryItem(
                rs.getObject("occurred_at", java.time.OffsetDateTime.class),
                rs.getString("ip"),
                rs.getString("user_agent"),
                rs.getString("result")
        );
    }

    private SessionItem mapSession(ResultSet rs, int rowNum) throws SQLException {
        return new SessionItem(
                rs.getString("session_id"),
                rs.getObject("login_at", java.time.OffsetDateTime.class),
                rs.getString("ip"),
                rs.getString("user_agent"),
                false
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
