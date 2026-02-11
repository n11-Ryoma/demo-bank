package com.example.ebank.publicinfo.repository.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FxRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public FxRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Double> loadFxToJpy() {
        Map<String, Double> map = new HashMap<>();
        jdbc.query("select currency, rate_to_jpy from fx_rates", rs -> {
            map.put(rs.getString("currency"), rs.getDouble("rate_to_jpy"));
        });
        return map;
    }
}
