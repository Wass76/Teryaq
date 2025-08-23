package com.Teryaq.moneybox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exchange Rate Request", example = """
{
  "fromCurrency": "USD",
  "toCurrency": "SYP",
  "rate": 2500.00,
  "source": "MANUAL",
  "notes": "Updated exchange rate for USD to SYP"
}
""")
public class ExchangeRateRequestDTO {
    
    @NotBlank(message = "From currency is required")
    @Schema(description = "Source currency code", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED", "SYP"})
    private String fromCurrency;
    
    @NotBlank(message = "To currency is required")
    @Schema(description = "Target currency code", example = "SYP", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED", "SYP"})
    private String toCurrency;
    
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    @Schema(description = "Exchange rate value", example = "2500.00")
    private BigDecimal rate;
    
    @Schema(description = "Source of the exchange rate", example = "MANUAL", 
            allowableValues = {"MANUAL", "API", "SYSTEM"})
    private String source = "MANUAL";
    
    @Schema(description = "Additional notes about the exchange rate", example = "Updated rate based on market conditions")
    private String notes;
}
