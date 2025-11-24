package com.example.eshop.auth.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.eshop.auth.entity.Role;

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



}
