package com.Teryaq.moneybox.dto;

import com.Teryaq.user.Enum.Currency;
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
    private Currency currency;
    private String paymentMethod; // CASH, CARD, etc.
}
