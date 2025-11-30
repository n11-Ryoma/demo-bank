package com.example.ebank.auth.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.auth.entity.Role;

@Repository
public class RoleRepositoryJdbc {

    private final JdbcTemplate jdbc;
    private final RoleRowMapper roleRowMapper = new RoleRowMapper();

    public RoleRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // 特定ユーザのロール一覧を取得
    public List<Role> findByUserId(Long userId) {
        String sql =
                "SELECT r.id, r.name " +
                "FROM roles r " +
                "INNER JOIN user_roles ur ON ur.role_id = r.id " +
                "WHERE ur.user_id = " + userId;

        return jdbc.query(sql,
                (rs, rowNum) -> {
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    role.setName(rs.getString("name"));
                    return role;
                }
        );  // ★ userId をここで渡さない
    }
    
    public Integer findRoleIdByName(String roleName) {
        String sql = "SELECT id FROM roles WHERE name = ?";
        return jdbc.queryForObject(sql, Integer.class, roleName);
    }

    public void insertUserRole(int userId, int roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        jdbc.update(sql, userId, roleId);
    }

    // ★ ユーザ名が既に存在するかチェック
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
