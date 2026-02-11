package com.example.ebank.requests.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.requests.dto.RequestDetailResponse;
import com.example.ebank.requests.dto.RequestItem;

@Repository
public class ServiceRequestRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public ServiceRequestRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RequestItem> findAllByUserId(Long userId, int limit) {
        String sql = """
            SELECT id, request_type, status, title, created_at, updated_at
            FROM service_requests
            WHERE user_id = :userId
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """;
        return jdbc.query(sql, Map.of("userId", userId, "limit", limit), this::mapItem);
    }

    public Optional<RequestDetailResponse> findByIdAndUserId(Long id, Long userId) {
        String sql = """
            SELECT id, request_type, status, title, detail, created_at, updated_at
            FROM service_requests
            WHERE id = :id AND user_id = :userId
            """;
        List<RequestDetailResponse> list = jdbc.query(sql, Map.of("id", id, "userId", userId), this::mapDetail);
        return list.stream().findFirst();
    }

    public Long insert(Long userId, String requestType, String status, String title, String detail) {
        String sql = """
            INSERT INTO service_requests
            (user_id, request_type, status, title, detail, created_at, updated_at)
            VALUES
            (:userId, :requestType, :status, :title, :detail, now(), now())
            RETURNING id
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("requestType", requestType)
                .addValue("status", status)
                .addValue("title", title)
                .addValue("detail", detail), Long.class);
    }

    private RequestItem mapItem(ResultSet rs, int rowNum) throws SQLException {
        RequestItem item = new RequestItem();
        item.setRequestId(rs.getLong("id"));
        item.setRequestType(rs.getString("request_type"));
        item.setStatus(rs.getString("status"));
        item.setTitle(rs.getString("title"));
        item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return item;
    }

    private RequestDetailResponse mapDetail(ResultSet rs, int rowNum) throws SQLException {
        RequestDetailResponse item = new RequestDetailResponse();
        item.setRequestId(rs.getLong("id"));
        item.setRequestType(rs.getString("request_type"));
        item.setStatus(rs.getString("status"));
        item.setTitle(rs.getString("title"));
        item.setDetail(rs.getString("detail"));
        item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return item;
    }
}
