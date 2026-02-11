package com.example.ebank.beneficiaries.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.beneficiaries.entity.Beneficiary;
import com.example.ebank.beneficiaries.repository.BeneficiaryRepository;

@Repository
public class BeneficiaryRepositoryJdbc implements BeneficiaryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BeneficiaryRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Beneficiary> findAllByUserId(Long userId) {
        String sql = """
            SELECT id, user_id, bank_name, branch_name, account_type, account_number,
                   account_holder_name, nickname, created_at
            FROM beneficiaries
            WHERE user_id = :userId
            ORDER BY id ASC
            """;
        return jdbc.query(sql, Map.of("userId", userId), this::mapRow);
    }

    @Override
    public Beneficiary save(Beneficiary beneficiary) {
        String sql = """
            INSERT INTO beneficiaries
            (user_id, bank_name, branch_name, account_type, account_number, account_holder_name, nickname)
            VALUES
            (:userId, :bankName, :branchName, :accountType, :accountNumber, :accountHolderName, :nickname)
            RETURNING id, user_id, bank_name, branch_name, account_type, account_number,
                      account_holder_name, nickname, created_at
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", beneficiary.getUserId())
                .addValue("bankName", beneficiary.getBankName())
                .addValue("branchName", beneficiary.getBranchName())
                .addValue("accountType", beneficiary.getAccountType())
                .addValue("accountNumber", beneficiary.getAccountNumber())
                .addValue("accountHolderName", beneficiary.getAccountHolderName())
                .addValue("nickname", beneficiary.getNickname());

        return jdbc.queryForObject(sql, params, this::mapRow);
    }

    @Override
    public Optional<Beneficiary> findByIdAndUserId(Long id, Long userId) {
        String sql = """
            SELECT id, user_id, bank_name, branch_name, account_type, account_number,
                   account_holder_name, nickname, created_at
            FROM beneficiaries
            WHERE id = :id AND user_id = :userId
            """;
        List<Beneficiary> list = jdbc.query(sql, Map.of("id", id, "userId", userId), this::mapRow);
        return list.stream().findFirst();
    }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM beneficiaries WHERE id = :id AND user_id = :userId";
        jdbc.update(sql, Map.of("id", id, "userId", userId));
    }

    private Beneficiary mapRow(ResultSet rs, int rowNum) throws SQLException {
        Beneficiary b = new Beneficiary();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setBankName(rs.getString("bank_name"));
        b.setBranchName(rs.getString("branch_name"));
        b.setAccountType(rs.getString("account_type"));
        b.setAccountNumber(rs.getString("account_number"));
        b.setAccountHolderName(rs.getString("account_holder_name"));
        b.setNickname(rs.getString("nickname"));
        b.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return b;
    }
}
