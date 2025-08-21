package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProductOverallDTOResponse {
    
    // Product Identification
    private Long productId;
    private String productName;
    private ProductType productType;
    private List<String> barcodes;
    
    // Stock Summary
    private Integer totalQuantity;           
    private Integer totalBonusQuantity;     
    private Double averagePurchasePrice;     
    private Double totalValue;               
    
    // Product Information
    private List<String> categories;
    private Float sellingPrice;
    
    // Stock Status
    private Integer minStockLevel;
    private Boolean hasExpiredItems;
    private Boolean hasExpiringSoonItems;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate earliestExpiryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latestExpiryDate;
    
    // Additional Info
    private Integer numberOfBatches;
    private Long pharmacyId;
}
