package com.example.ebank.publicnotices.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.example.ebank.publicnotices.entity.PublicNotice;

public class PublicNoticeRowMapper implements RowMapper<PublicNotice> {
    @Override
    public PublicNotice mapRow(ResultSet rs, int rowNum) throws SQLException {
        PublicNotice n = new PublicNotice();
        n.setId(rs.getLong("id"));
        n.setCategory(rs.getString("category"));
        n.setTitle(rs.getString("title"));
        n.setSummary(rs.getString("summary"));
        n.setBodyMarkdown(rs.getString("body_markdown"));
        n.setBodyHtml(rs.getString("body_html"));
        n.setSeverity(rs.getString("severity"));
        n.setStatusLabel(rs.getString("status_label"));
        n.setPublished(rs.getBoolean("is_published"));

        // timestamptz -> OffsetDateTime
        n.setPublishFrom(rs.getObject("publish_from", OffsetDateTime.class));
        n.setPublishUntil(rs.getObject("publish_until", OffsetDateTime.class));

        n.setRenderProfile(rs.getString("render_profile"));
        n.setRenderData(rs.getString("render_data"));

        n.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        n.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return n;
    }
}
