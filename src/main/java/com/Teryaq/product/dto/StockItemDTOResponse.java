package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockItemDTOResponse {
    private Long id;
    private Long productId;
    private String productName;
    private ProductType productType;
    private Integer quantity;
    private Integer bonusQty;
    private LocalDate expiryDate;
    private String batchNo;
    private Double actualPurchasePrice;
    private LocalDateTime dateAdded;
    private Long addedBy;
    private Long purchaseInvoiceId;
    
    // معلومات إضافية
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;
} 