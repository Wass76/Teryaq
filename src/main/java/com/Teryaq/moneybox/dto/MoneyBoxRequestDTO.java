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
public class MoneyBoxRequestDTO {
    private BigDecimal openingBalance;
    private String notes;
}
