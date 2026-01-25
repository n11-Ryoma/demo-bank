package com.example.ebank.accounts.dto;

import java.time.OffsetDateTime;
import java.util.EnumSet;

import com.example.ebank.model.TransactionType;

public class TransactionHistoryFilter {
    private OffsetDateTime from;                 // 例: 2026-01-01T00:00:00+09:00
    private OffsetDateTime to;                   // 例: 2026-02-01T00:00:00+09:00 (toは排他的推奨)
    private EnumSet<TransactionType> types;      // DEPOSIT/WITHDRAW/TRANSFER ...
    private Long minAmount;
    private Long maxAmount;
    private String relatedAccountNumber;         // 相手口座
    private String descriptionKeyword;           // 説明の部分一致
	public OffsetDateTime getFrom() {
		return from;
	}
	public void setFrom(OffsetDateTime from) {
		this.from = from;
	}
	public OffsetDateTime getTo() {
		return to;
	}
	public void setTo(OffsetDateTime to) {
		this.to = to;
	}
	public EnumSet<TransactionType> getTypes() {
		return types;
	}
	public void setTypes(EnumSet<TransactionType> types) {
		this.types = types;
	}
	public Long getMinAmount() {
		return minAmount;
	}
	public void setMinAmount(Long minAmount) {
		this.minAmount = minAmount;
	}
	public Long getMaxAmount() {
		return maxAmount;
	}
	public void setMaxAmount(Long maxAmount) {
		this.maxAmount = maxAmount;
	}
	public String getRelatedAccountNumber() {
		return relatedAccountNumber;
	}
	public void setRelatedAccountNumber(String relatedAccountNumber) {
		this.relatedAccountNumber = relatedAccountNumber;
	}
	public String getDescriptionKeyword() {
		return descriptionKeyword;
	}
	public void setDescriptionKeyword(String descriptionKeyword) {
		this.descriptionKeyword = descriptionKeyword;
	}


}

