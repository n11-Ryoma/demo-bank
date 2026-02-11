package com.example.ebank.limits.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.limits.dto.LimitsResponse;

@Repository
public class LimitsRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public LimitsRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<LimitsResponse> findByUserId(Long userId) {
        String sql = """
            SELECT transfer_limit_yen, atm_withdraw_limit_yen, updated_at
            FROM user_limits
            WHERE user_id = :userId
            """;
        List<LimitsResponse> list = jdbc.query(sql, Map.of("userId", userId), this::mapRow);
        return list.stream().findFirst();
    }

    public LimitsResponse upsert(Long userId, long transferLimitYen, long atmWithdrawLimitYen) {
        String sql = """
            INSERT INTO user_limits (user_id, transfer_limit_yen, atm_withdraw_limit_yen, updated_at)
            VALUES (:userId, :transferLimitYen, :atmWithdrawLimitYen, now())
            ON CONFLICT (user_id) DO UPDATE SET
                transfer_limit_yen = EXCLUDED.transfer_limit_yen,
                atm_withdraw_limit_yen = EXCLUDED.atm_withdraw_limit_yen,
                updated_at = now()
            RETURNING transfer_limit_yen, atm_withdraw_limit_yen, updated_at
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("transferLimitYen", transferLimitYen)
                .addValue("atmWithdrawLimitYen", atmWithdrawLimitYen), this::mapRow);
    }

    private LimitsResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        LimitsResponse response = new LimitsResponse();
        response.setTransferLimitYen(rs.getLong("transfer_limit_yen"));
        response.setAtmWithdrawLimitYen(rs.getLong("atm_withdraw_limit_yen"));
        response.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return response;
    }
}
