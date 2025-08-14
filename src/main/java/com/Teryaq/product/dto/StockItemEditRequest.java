package com.Teryaq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "edit stock quantity request", example= """
{
    "quantity": 10,
    "reasonCode": "Received Shipment",
    "additionalNotes": "تم استلام شحنة جديدة"
}
        """)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemEditRequest {

    @Schema(description = "quantity", example = "10")
    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
    
    @Schema(description = "reason code", example = "Received Shipment")
    @NotBlank(message = "reason code is required")
    private String reasonCode;
    
    @Schema(description = "additional notes", example = "تم استلام شحنة جديدة")
    private String additionalNotes;
} 