package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String batchNo;
    private Double actualPurchasePrice;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAdded;
    private Long addedBy;
    private Long purchaseInvoiceId;
    
    // معلومات إضافية
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;
} 