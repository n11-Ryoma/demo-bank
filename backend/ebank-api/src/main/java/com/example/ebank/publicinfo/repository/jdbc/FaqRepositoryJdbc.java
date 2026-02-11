package com.example.ebank.publicinfo.repository.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.FaqItem;

@Repository
public class FaqRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public FaqRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SearchResult search(String query, String category, int page, int size) {
        StringBuilder where = new StringBuilder(" where 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            where.append(" and lower(category)=lower(?)");
            params.add(category);
        }
        if (query != null && !query.isBlank()) {
            where.append(" and (lower(question) like ? or lower(answer) like ? or lower(tags) like ?)");
            String like = like(query);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        long total = jdbc.queryForObject("select count(*) from faq_items" + where, params.toArray(), Long.class);

        String sql = "select id, category, question, answer, tags from faq_items" + where +
                " order by id asc limit ? offset ?";
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(size);
        listParams.add((page - 1) * size);

        List<FaqItem> items = jdbc.query(sql, listParams.toArray(), faqRowMapper());
        return new SearchResult(items, total);
    }

    private RowMapper<FaqItem> faqRowMapper() {
        return (rs, rowNum) -> new FaqItem(
                rs.getString("id"),
                rs.getString("category"),
                rs.getString("question"),
                rs.getString("answer"),
                splitCsv(rs.getString("tags")));
    }

    private List<String> splitCsv(String csv) {
        List<String> list = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return list;
        }
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private String like(String q) {
        return "%" + q.toLowerCase(Locale.ROOT) + "%";
    }

    public static class SearchResult {
        private final List<FaqItem> items;
        private final long total;

        public SearchResult(List<FaqItem> items, long total) {
            this.items = items;
            this.total = total;
        }

        public List<FaqItem> getItems() {
            return items;
        }

        public long getTotal() {
            return total;
        }
    }
}
