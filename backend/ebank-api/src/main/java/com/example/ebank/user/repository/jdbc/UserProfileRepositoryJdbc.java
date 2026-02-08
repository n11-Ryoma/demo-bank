package com.example.ebank.user.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.user.entity.UserProfile;
@Repository
public class UserProfileRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public UserProfileRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UserProfile findByUserId(String userId) {
        String sql = """
            SELECT user_id, name_kanji, name_kana, birth_date, gender,
                   phone, postal_code, address, my_number
            FROM user_profile
            WHERE user_id = :userId
            """;

        return jdbc.query(sql, Map.of("userId", userId), rs ->
            rs.next() ? map(rs) : null
        );
    }

    private UserProfile map(ResultSet rs) throws SQLException {
        UserProfile p = new UserProfile();
        p.setUserId(rs.getLong("user_id"));
        p.setNameKanji(rs.getString("name_kanji"));
        p.setNameKana(rs.getString("name_kana"));
        p.setBirthDate(rs.getDate("birth_date").toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setPostalCode(rs.getString("postal_code"));
        p.setAddress(rs.getString("address"));
        p.setMyNumber(rs.getString("my_number"));
        return p;
    }
}

