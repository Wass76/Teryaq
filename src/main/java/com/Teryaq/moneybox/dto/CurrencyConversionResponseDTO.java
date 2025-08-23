package com.Teryaq.moneybox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Currency Conversion Response", example = """
{
  "fromAmount": 100.00,
  "fromCurrency": "USD",
  "toAmount": 250000.00,
  "toCurrency": "SYP",
  "rate": 2500.00
}
""")
public class CurrencyConversionResponseDTO {
    
    @Schema(description = "Original amount", example = "100.00")
    private BigDecimal fromAmount;
    
    @Schema(description = "Source currency", example = "USD")
    private String fromCurrency;
    
    @Schema(description = "Converted amount", example = "250000.00")
    private BigDecimal toAmount;
    
    @Schema(description = "Target currency", example = "SYP")
    private String toCurrency;
    
    @Schema(description = "Exchange rate used", example = "2500.00")
    private BigDecimal rate;
}
