package com.example.ebank.publicinfo.repository.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FxRepositoryJdbc {

    private static final Logger log = LogManager.getLogger(FxRepositoryJdbc.class);
    private final JdbcTemplate jdbc;

    public FxRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Double> loadFxToJpy() {
        log.info("loadFxToJpy called");
        Map<String, Double> map = new HashMap<>();
        jdbc.query("select currency, rate_to_jpy from fx_rates", rs -> {
            map.put(rs.getString("currency"), rs.getDouble("rate_to_jpy"));
        });
        return map;
    }
}
