package com.Teryaq.moneybox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private Long moneyBoxId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String referenceType;
    private Long referenceId;
    private String referenceNumber;
    private String notes;
    private String receiptNumber;
}
