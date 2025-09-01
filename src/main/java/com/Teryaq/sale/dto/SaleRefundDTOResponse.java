package com.Teryaq.sale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sale Refund Response")
public class SaleRefundDTOResponse {
    
    @Schema(description = "Refund ID", example = "1")
    private Long refundId;
    
    @Schema(description = "Sale invoice ID", example = "1")
    private Long saleInvoiceId;
    
    @Schema(description = "Total refund amount", example = "150.0")
    private Float totalRefundAmount;
    
    @Schema(description = "Refund reason", example = "Customer request")
    private String refundReason;
    
    @Schema(description = "Refund date", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "dd-MM-yyyy, HH:mm:ss")
    private LocalDateTime refundDate;
    
    @Schema(description = "Refunded items")
    private List<SaleRefundItemDTOResponse> refundedItems;
    
    @Schema(description = "Stock restored", example = "true")
    private Boolean stockRestored;
    

  
 
}
