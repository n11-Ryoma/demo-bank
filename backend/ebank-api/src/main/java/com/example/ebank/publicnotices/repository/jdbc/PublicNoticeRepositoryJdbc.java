package com.example.ebank.publicnotices.repository.jdbc;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicnotices.entity.PublicNotice;

@Repository
public class PublicNoticeRepositoryJdbc {

    private final JdbcTemplate jdbcTemplate;
    private final PublicNoticeRowMapper rowMapper = new PublicNoticeRowMapper();

    public PublicNoticeRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PublicNotice> findVisible(Optional<String> categoryOpt, int limit) {
        String baseSql = """
            select *
            from public_notices
            where is_published = true
              and publish_from <= now()
              and (publish_until is null or now() <= publish_until)
        """;

        String orderSql = """
            order by
              case severity
                when 'CRITICAL' then 1
                when 'WARN' then 2
                else 3
              end asc,
              publish_from desc
            limit ?
        """;

        if (categoryOpt.isPresent() && !categoryOpt.get().isBlank()) {
            String sql = baseSql + " and category = ? " + orderSql;
            return jdbcTemplate.query(sql, rowMapper, categoryOpt.get(), limit);
        } else {
            String sql = baseSql + " " + orderSql;
            return jdbcTemplate.query(sql, rowMapper, limit);
        }
    }

    public Optional<PublicNotice> findVisibleById(long id) {
        String sql = """
            select *
            from public_notices
            where id = ?
              and is_published = true
              and publish_from <= now()
              and (publish_until is null or now() <= publish_until)
        """;
        List<PublicNotice> r = jdbcTemplate.query(sql, rowMapper, id);
        return r.stream().findFirst();
    }
}
