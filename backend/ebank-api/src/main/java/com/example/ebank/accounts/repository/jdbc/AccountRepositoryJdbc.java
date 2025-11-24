package com.example.ebank.accounts.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ebank.accounts.entity.Account;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;

//com.example.eshop.accounts.repository.jdbc.AccountRepositoryJdbc
@Repository
public class AccountRepositoryJdbc {
	private static final Logger log =LogManager.getLogger(UserRepositoryJdbc.class);
    	private final JdbcTemplate jdbc;
	
	public AccountRepositoryJdbc(JdbcTemplate jdbc) {
	    this.jdbc = jdbc;
	}
	public List<Account> findByAccountNumber(String accountNumber) {
	    String sql = "SELECT id, user_id, account_number, balance FROM accounts " +
	                 "WHERE account_number = '" + accountNumber + "'";

	    log.info("### Executing SQL: {}", sql);

	    return jdbc.query(sql, this::mapRow);
	}

	
	private Account mapRow(ResultSet rs, int rowNum) throws SQLException {
	    Account a = new Account();
	    a.setId(rs.getLong("id"));
	    a.setUserId(rs.getLong("user_id"));
	    a.setAccountNumber(rs.getString("account_number"));
	    a.setBalance(rs.getLong("balance"));
	    return a;
	}
}

