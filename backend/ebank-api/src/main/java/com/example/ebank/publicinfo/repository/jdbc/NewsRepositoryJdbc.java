package com.example.ebank.publicinfo.repository.jdbc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.NewsDetail;

@Repository
public class NewsRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public NewsRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SearchResult list(String category, String q, int page, int size) {
        StringBuilder where = new StringBuilder(" where 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            where.append(" and lower(category)=lower(?)");
            params.add(category);
        }
        if (q != null && !q.isBlank()) {
            where.append(" and (lower(title) like ? or lower(summary) like ? or lower(body) like ?)");
            String like = like(q);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        long total = jdbc.queryForObject("select count(*) from news_items" + where,
                params.toArray(), Long.class);

        String sql = "select id, category, title, summary, body, published_at, updated_at " +
                "from news_items" + where + " order by published_at desc limit ? offset ?";

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(size);
        listParams.add((page - 1) * size);

        List<NewsDetail> items = jdbc.query(sql, listParams.toArray(), newsRowMapper());
        return new SearchResult(items, total);
    }

    public NewsDetail findById(String id) {
        List<NewsDetail> items = jdbc.query(
                "select id, category, title, summary, body, published_at, updated_at from news_items where id = ?",
                newsRowMapper(), id);
        return items.isEmpty() ? null : items.get(0);
    }

    private RowMapper<NewsDetail> newsRowMapper() {
        return (rs, rowNum) -> {
            OffsetDateTime publishedAt = rs.getObject("published_at", OffsetDateTime.class);
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            return new NewsDetail(
                    rs.getString("id"),
                    rs.getString("category"),
                    rs.getString("title"),
                    rs.getString("summary"),
                    rs.getString("body"),
                    publishedAt == null ? null : publishedAt.toString(),
                    updatedAt == null ? null : updatedAt.toString());
        };
    }

    private String like(String q) {
        return "%" + q.toLowerCase(Locale.ROOT) + "%";
    }

    public static class SearchResult {
        private final List<NewsDetail> items;
        private final long total;

        public SearchResult(List<NewsDetail> items, long total) {
            this.items = items;
            this.total = total;
        }

        public List<NewsDetail> getItems() {
            return items;
        }

        public long getTotal() {
            return total;
        }
    }
}
