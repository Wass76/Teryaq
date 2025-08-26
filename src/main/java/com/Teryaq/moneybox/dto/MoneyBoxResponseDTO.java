package com.Teryaq.moneybox.dto;

import com.Teryaq.moneybox.enums.MoneyBoxStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
