package com.example.ebank.publicinfo.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.RateItem;

@Repository
public class RatesRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public RatesRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RateItem> findRates(String category) {
        if (category == null || category.isBlank()) {
            return jdbc.query("select category, product, rate_percent, term, note from rate_items order by category, product",
                    rateRowMapper());
        }
        return jdbc.query("select category, product, rate_percent, term, note from rate_items where lower(category)=lower(?) order by product",
                rateRowMapper(), category);
    }

    private RowMapper<RateItem> rateRowMapper() {
        return (rs, rowNum) -> new RateItem(
                rs.getString("category"),
                rs.getString("product"),
                rs.getDouble("rate_percent"),
                rs.getString("term"),
                rs.getString("note"));
    }
}
