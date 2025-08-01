package com.Teryaq.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayDebtDTORequest {
    private Long debtId;
    private BigDecimal paymentAmount;
    private String notes;
} 