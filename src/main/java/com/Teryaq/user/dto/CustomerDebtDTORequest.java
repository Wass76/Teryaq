package com.Teryaq.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Customer Debt Request", example = """
{
  "customerId": 1,
  "amount": 150.50,
  "dueDate": "2024-12-31",
  "notes": "Payment for medication"
}
""")
public class CustomerDebtDTORequest {
    
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;
    
    @Schema(description = "Debt amount", example = "150.50")
    private Float amount;

    @Schema(description = "Due date for the debt", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    @Schema(description = "Additional notes about the debt", example = "Payment for medication")
    private String notes;
} 