package com.example.ebank.auth.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.auth.entity.User;

@Repository
public class UserRepositoryJdbc {

    private static final Logger log =
        LogManager.getLogger(UserRepositoryJdbc.class);

    private final JdbcTemplate jdbc;

    public UserRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // SQLi 実習用
    public List<User> findByUsernameAndPasswordVuln(String username, String password) {

        String sql =
            "SELECT id, username, password FROM users " +
            "WHERE username = '" + username + "' " +
            "AND password = '" + password + "'";

        log.info("### Executing SQL: {}", sql);

        return jdbc.query(sql, this::mapRow);
    }
    
    private User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        return u;
    }

    public int insertUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING id";
        return jdbc.queryForObject(sql, Integer.class, username, password);
    }

    public List<User> findByUsername(String username) {
        String sql = "SELECT id, username, password FROM users WHERE username = "+ username;
        return jdbc.query(sql, this::mapRow);
    }
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    // ★ ユーザ作成して新しい ID を返す（パスワードはデモ用で平文のまま）
    public Long createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING id";
        return jdbc.queryForObject(sql, Long.class, username, password);
    }

}
