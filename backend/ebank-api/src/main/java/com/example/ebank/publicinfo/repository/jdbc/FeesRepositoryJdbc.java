package com.example.ebank.publicinfo.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.publicinfo.dto.FeeItem;

@Repository
public class FeesRepositoryJdbc {

    private final JdbcTemplate jdbc;

    public FeesRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<FeeItem> findFees(String service) {
        if (service == null || service.isBlank()) {
            return jdbc.query("select service, channel, amount_yen, note from fee_items order by service, channel",
                    feeRowMapper());
        }
        return jdbc.query("select service, channel, amount_yen, note from fee_items where lower(service)=lower(?) order by channel",
                feeRowMapper(), service);
    }

    private RowMapper<FeeItem> feeRowMapper() {
        return (rs, rowNum) -> new FeeItem(
                rs.getString("service"),
                rs.getString("channel"),
                rs.getInt("amount_yen"),
                rs.getString("note"));
    }
}
