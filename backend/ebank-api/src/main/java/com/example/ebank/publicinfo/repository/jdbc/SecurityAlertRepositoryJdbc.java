package com.example.ebank.publicinfo.repository.jdbc;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.SecurityAlertItem;

@Repository
public class SecurityAlertRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public SecurityAlertRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SecurityAlertItem> list(String tag, int limit) {
        if (tag == null || tag.isBlank()) {
            return jdbc.query(
                    "select id, title, tag, risk_level, recent_count, tip, updated_at " +
                    "from security_alerts order by recent_count desc limit ?",
                    alertRowMapper(), limit);
        }
        return jdbc.query(
                "select id, title, tag, risk_level, recent_count, tip, updated_at " +
                "from security_alerts where lower(tag)=lower(?) order by recent_count desc limit ?",
                alertRowMapper(), tag, limit);
    }

    private RowMapper<SecurityAlertItem> alertRowMapper() {
        return (rs, rowNum) -> {
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            return new SecurityAlertItem(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("tag"),
                    rs.getString("risk_level"),
                    rs.getInt("recent_count"),
                    rs.getString("tip"),
                    updatedAt == null ? null : updatedAt.toString());
        };
    }
}
