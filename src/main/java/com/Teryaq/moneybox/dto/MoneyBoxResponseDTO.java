package com.Teryaq.moneybox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxResponseDTO {
    private Long id;
    private Long pharmacyId;
    private LocalDate businessDate;
    private String status;
    private String periodType;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal netCashFlow;
    private LocalDateTime openedAt;
    private String openedBy;
    private String notes;
}
