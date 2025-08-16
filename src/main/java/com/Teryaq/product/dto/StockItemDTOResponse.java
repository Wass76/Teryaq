package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
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
    
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;
    
    private Long pharmacyId;
    private String purchaseInvoiceNumber;
    
    // Constructor مخصص للبحث من خلال Query
    public StockItemDTOResponse(Long id, Long productId, String productName, ProductType productType, 
                               Integer quantity, Integer bonusQty, LocalDate expiryDate, String batchNo, 
                               Double actualPurchasePrice, LocalDate dateAdded, Long addedBy, 
                               Long purchaseInvoiceId, Boolean isExpired, Boolean isExpiringSoon, 
                               Integer daysUntilExpiry, Long pharmacyId, 
                               String purchaseInvoiceNumber) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.bonusQty = bonusQty;
        this.expiryDate = expiryDate;
        this.batchNo = batchNo;
        this.actualPurchasePrice = actualPurchasePrice;
        this.dateAdded = dateAdded;
        this.addedBy = addedBy;
        this.purchaseInvoiceId = purchaseInvoiceId;
        
        // حساب القيم الحقيقية
        if (expiryDate != null) {
            LocalDate today = LocalDate.now();
            this.isExpired = expiryDate.isBefore(today);
            this.isExpiringSoon = expiryDate.isBefore(today.plusDays(30)) && !expiryDate.isBefore(today);
            this.daysUntilExpiry = (int) java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
        } else {
            this.isExpired = false;
            this.isExpiringSoon = false;
            this.daysUntilExpiry = null;
        }
        
        this.pharmacyId = pharmacyId;
        this.purchaseInvoiceNumber = purchaseInvoiceNumber;
    }
} 