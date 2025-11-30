package com.example.ebank.address.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.address.dto.CurrentAddressResponse;

@Repository
public class AddressRepositoryJdbc {

    private final NamedParameterJdbcTemplate jdbc;

    public AddressRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 住所変更リクエストを INSERT（パス＋バイナリの両方を保存）
     */
    public void insertAddressChangeRequest(
            Long userId,
            String postalCode,
            String prefecture,
            String city,
            String addressLine1,
            String addressLine2,
            String proofFilePath,   // ログ用のパス（いらなければ null でもOK）
            byte[] proofFileData    // DB にしまうバイナリ
    ) {

        String sql = """
            INSERT INTO address_change_requests
                (user_id, postal_code, prefecture, city,
                 address_line1, address_line2,
                 proof_file_path, proof_file_data,
                 status, created_at)
            VALUES
                (:userId, :postal, :pref, :city,
                 :line1, :line2,
                 :filePath, :fileData,
                 'PENDING', NOW())
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("postal", postalCode);
        params.put("pref", prefecture);
        params.put("city", city);
        params.put("line1", addressLine1);
        params.put("line2", addressLine2);
        params.put("filePath", proofFilePath);
        params.put("fileData", proofFileData);

        jdbc.update(sql, params);
    }
    public CurrentAddressResponse findLatestAddressByUserId(Long userId) {

        String sql = """
            SELECT postal_code, prefecture, city, address_line1, address_line2
            FROM address_change_requests
            WHERE user_id = :userId
            ORDER BY created_at DESC
            LIMIT 1
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        return jdbc.query(sql, params, rs -> {
            if (!rs.next()) {
                return null;
            }
            return mapToCurrentAddress(rs);
        });
    }

    private CurrentAddressResponse mapToCurrentAddress(ResultSet rs) throws SQLException {
        return new CurrentAddressResponse(
                rs.getString("postal_code"),
                rs.getString("prefecture"),
                rs.getString("city"),
                rs.getString("address_line1"),
                rs.getString("address_line2")
        );
    }
}
