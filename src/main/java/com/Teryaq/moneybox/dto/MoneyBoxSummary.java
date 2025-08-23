package com.Teryaq.moneybox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxSummary {
    private Long moneyBoxId;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal netCashFlow;
    private Integer transactionCount;
    private LocalDateTime lastTransactionDate;
}
