package com.Teryaq.moneybox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTransactionRequestDTO {
    private Long pharmacyId;
    private Long purchaseId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // CASH, CARD, etc.
}
