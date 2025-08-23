package com.Teryaq.moneybox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Exchange Rate Response", example = """
{
  "id": 1,
  "fromCurrency": "USD",
  "toCurrency": "SYP",
  "rate": 2500.00,
  "effectiveFrom": "2024-01-01T00:00:00",
  "effectiveTo": null,
  "isActive": true,
  "source": "MANUAL",
  "notes": "Updated rate based on market conditions",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
""")
public class ExchangeRateResponseDTO {
    
    @Schema(description = "Exchange rate ID", example = "1")
    private Long id;
    
    @Schema(description = "Source currency code", example = "USD")
    private String fromCurrency;
    
    @Schema(description = "Target currency code", example = "SYP")
    private String toCurrency;
    
    @Schema(description = "Exchange rate value", example = "2500.00")
    private BigDecimal rate;
    
    @Schema(description = "When this rate becomes effective", example = "2024-01-01T00:00:00")
    private LocalDateTime effectiveFrom;
    
    @Schema(description = "When this rate expires (null if currently active)", example = "2024-12-31T23:59:59")
    private LocalDateTime effectiveTo;
    
    @Schema(description = "Whether this rate is currently active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Source of the exchange rate", example = "MANUAL")
    private String source;
    
    @Schema(description = "Additional notes", example = "Updated rate based on market conditions")
    private String notes;
    
    @Schema(description = "When this record was created", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "When this record was last updated", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
