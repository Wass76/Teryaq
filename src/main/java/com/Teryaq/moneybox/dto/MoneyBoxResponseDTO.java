package com.Teryaq.moneybox.dto;

import com.Teryaq.moneybox.enums.MoneyBoxStatus;
import com.Teryaq.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxResponseDTO {
    
    private Long id;
    private Long pharmacyId;
    private BigDecimal currentBalance;
    private BigDecimal initialBalance;
    private LocalDateTime lastReconciled;
    private BigDecimal reconciledBalance;
    private MoneyBoxStatus status;
    private String currency; // Always SYP for consistency
    
    // Currency conversion summary
    private Currency baseCurrency = Currency.SYP;
    private BigDecimal totalBalanceInSYP;
    private BigDecimal totalBalanceInUSD;
    private BigDecimal totalBalanceInEUR;
    
    // Transaction summary
    private List<MoneyBoxTransactionResponseDTO> recentTransactions;
    private Integer totalTransactionCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
