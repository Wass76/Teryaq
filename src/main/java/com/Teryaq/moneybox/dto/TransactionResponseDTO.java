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
public class TransactionResponseDTO {
    private Long id;
    private Long moneyBoxId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountInSYP;
    private String description;
    private String referenceType;
    private String referenceNumber;
    private String status;
    private LocalDateTime transactionDate;
    private String employeeName;
}
