package com.example.ebank.me.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.me.dto.MeResponse;

@Repository
public class MeRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public MeRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public MeResponse findByUsername(String username) {
        String sql = """
            SELECT u.id AS user_id,
                   u.username,
                   u.email,
                   p.name_kanji,
                   p.name_kana,
                   p.phone,
                   p.postal_code,
                   p.address
            FROM users u
            LEFT JOIN user_profile p ON p.user_id = u.id
            WHERE u.username = :username
            LIMIT 1
            """;

        List<MeResponse> list = jdbc.query(
                sql,
                java.util.Map.of("username", username),
                this::mapRow
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private MeResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        MeResponse response = new MeResponse();
        response.setUserId(rs.getLong("user_id"));
        response.setUsername(rs.getString("username"));
        response.setEmail(rs.getString("email"));
        response.setNameKanji(rs.getString("name_kanji"));
        response.setNameKana(rs.getString("name_kana"));
        response.setPhone(rs.getString("phone"));
        response.setPostalCode(rs.getString("postal_code"));
        response.setAddress(rs.getString("address"));
        return response;
    }
}
