package com.example.ebank.accounts.repository.jdbc;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.ebank.accounts.dto.TransactionHistoryItem;
import com.example.ebank.accounts.entity.Account;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;
import com.example.ebank.model.TransactionType;

//com.example.eshop.accounts.repository.jdbc.AccountRepositoryJdbc
@Repository
public class AccountRepositoryJdbc {

    private final JdbcTemplate jdbc;
    private static final Logger log =
            LogManager.getLogger(UserRepositoryJdbc.class);
    public AccountRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Accountマッピング
    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> {
        Account a = new Account();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setBranchCode(rs.getString("branch_code"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setBalance(rs.getLong("balance"));
        // a.setIsMain(true); // フィールドが残ってるなら固定でtrueにしてもOK
        return a;
    };


    // ── 口座取得系 ─────────────────

    public Optional<Account> findMainByUsername(String username) {
        String sql = 
        	    "SELECT a.id, a.user_id, a.branch_code, a.account_number, a.balance " +
        	    "FROM accounts a " +
        	    "JOIN users u ON a.user_id = u.id " +
        	    "WHERE u.username = '" + username + "'";
        //var list = jdbc.query(sql, accountRowMapper, username);
        var list = jdbc.query(sql, accountRowMapper);
        return list.stream().findFirst();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
    		String sql = """
    		    SELECT id, user_id, branch_code, account_number, balance
    		    FROM accounts
    		    WHERE account_number = '""" + accountNumber + "'";

        
        //var list = jdbc.query(sql, accountRowMapper, accountNumber);
        var list = jdbc.query(sql, accountRowMapper);
        return list.stream().findFirst();
    }

    public List<Account> findByUsername(String username) {
        String sql = """
            SELECT a.id, a.user_id, a.branch_code, a.account_number, a.balance
            FROM accounts a
            JOIN users u ON a.user_id = u.id
            WHERE u.username = ?
            ORDER BY a.id ASC
            """;
        return jdbc.query(sql, accountRowMapper, username);
    }

    public Optional<Account> findByIdAndUsername(long accountId, String username) {
        String sql = """
            SELECT a.id, a.user_id, a.branch_code, a.account_number, a.balance
            FROM accounts a
            JOIN users u ON a.user_id = u.id
            WHERE a.id = ? AND u.username = ?
            """;
        var list = jdbc.query(sql, accountRowMapper, accountId, username);
        return list.stream().findFirst();
    }

    public java.time.OffsetDateTime findOpenedAtByAccountId(long accountId) {
        String sql = """
            SELECT created_at
            FROM transactions
            WHERE account_id = ? AND type = 'OPEN'
            ORDER BY created_at ASC
            LIMIT 1
            """;
        var list = jdbc.query(sql, (rs, rowNum) -> rs.getObject("created_at", java.time.OffsetDateTime.class), accountId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void updateBalance(long accountId, long newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        jdbc.update(sql, newBalance, accountId);
    }

    // ── 取引履歴系 ─────────────────

    public void insertTransaction(long accountId,
                                  TransactionType type,
                                  long amount,
                                  long balanceAfter,
                                  String relatedAccountNumber,
                                  String description) {
        String sql = """
            INSERT INTO transactions
              (account_id, type, amount, balance_after, related_account_number, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        jdbc.update(sql,
                accountId,
                type.name(),
                amount,
                balanceAfter,
                relatedAccountNumber,
                description
        );
    }

    // usernameに紐づく全口座の履歴（シンプル版：最近n件）
    public List<TransactionHistoryItem> findHistoryByUsername(
        String username, int limit, int offset) {

	    	String sql =
	    		    "SELECT t.account_id, " +
	    		    "       a.account_number, " +
	    		    "       t.type, " +
	    		    "       t.amount, " +
	    		    "       t.balance_after, " +
	    		    "       t.related_account_number, " +
	    		    "       t.description, " +
	    		    "       t.created_at " +
	    		    "FROM transactions t " +
	    		    "JOIN accounts a ON t.account_id = a.id " +
	    		    "JOIN users u ON a.user_id = u.id " +
	    		    "WHERE u.username = '" + username + "' " +   
	    		    "ORDER BY t.created_at DESC, t.id DESC " ;/*+
	    		    "LIMIT " + limit + " " +
	    		    "OFFSET " + offset;*/
   
    	log.debug("### Executing SQL: {}", sql);
	    	return jdbc.query(sql, (rs, rowNum) -> {
	    	    TransactionHistoryItem item = new TransactionHistoryItem();
	    	    item.setAccountNumber(rs.getString("account_number"));
	    	    item.setType(TransactionType.valueOf(rs.getString("type")));
	    	    item.setAmount(rs.getLong("amount"));
	    	    item.setBalanceAfter(rs.getLong("balance_after"));
	    	    item.setRelatedAccountNumber(rs.getString("related_account_number"));
	    	    item.setDescription(rs.getString("description"));
	    	    item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class)); // ここが落ちたらTimestamp版へ
	    	    return item;
	    	});

    }
    public List<TransactionHistoryItem> findHistoryByUsernameFindStr(
            String username, int limit, int offset,String findStr) {

    	    	String sql =
    	    		    "SELECT t.account_id, " +
    	    		    "       a.account_number, " +
    	    		    "       t.type, " +
    	    		    "       t.amount, " +
    	    		    "       t.balance_after, " +
    	    		    "       t.related_account_number, " +
    	    		    "       t.description, " +
    	    		    "       t.created_at " +
    	    		    "FROM transactions t " +
    	    		    "JOIN accounts a ON t.account_id = a.id " +
    	    		    "JOIN users u ON a.user_id = u.id " +
    	    		    "WHERE u.username = '" + username + "' AND t.description LIKE '%" +findStr + "%' " ;
       
    	    	log.debug("### Executing SQL: {}", sql);
    	    	return jdbc.query(sql, (rs, rowNum) -> {
    	    	    TransactionHistoryItem item = new TransactionHistoryItem();
    	    	    item.setAccountNumber(rs.getString("account_number"));
    	    	    item.setType(TransactionType.valueOf(rs.getString("type")));
    	    	    item.setAmount(rs.getLong("amount"));
    	    	    item.setBalanceAfter(rs.getLong("balance_after"));
    	    	    item.setRelatedAccountNumber(rs.getString("related_account_number"));
    	    	    item.setDescription(rs.getString("description"));
    	    	    item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class)); // ここが落ちたらTimestamp版へ
    	    	    return item;
    	    	});

        }
    // ユーザ用の口座を1つ自動作成して、口座番号を返す
    public Account createMainAccountForUser(Long userId) {

        String branchCode = "0001"; // テスト用固定
        String accountNumber = String.format("%07d", userId);

        String sql = """
            INSERT INTO accounts (user_id, branch_code, account_number, balance)
            VALUES (?, ?, ?, 0)
            RETURNING id, user_id, branch_code, account_number, balance
            """;

        return jdbc.queryForObject(sql, accountRowMapper, userId, branchCode, accountNumber);
    }

}


