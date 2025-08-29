package com.Teryaq.moneybox.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionResponseDTO {
    
    private BigDecimal originalAmount;
    private Currency fromCurrency;
    private BigDecimal convertedAmount;
    private Currency toCurrency;
    private BigDecimal exchangeRate;
    private LocalDateTime conversionTime;
    private String rateSource;
}
