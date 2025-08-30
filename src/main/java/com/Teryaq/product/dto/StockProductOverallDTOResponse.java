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
    private Long id;
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
    
    // NEW: Currency Support Fields (Optional - only populated when currency conversion is requested)
    private String requestedCurrency;
    private Boolean pricesConverted;
    
    // NEW: Converted Price Fields (Optional - only populated when currency conversion is requested)
    private Double averagePurchasePriceConverted;
    private Double totalValueConverted;
    private Float sellingPriceConverted;
    
    // NEW: Exchange Rate Information (Optional - only populated when currency conversion is requested)
    private Double exchangeRate;
    private String conversionTimestamp;
    private String rateSource;
    
    /**
     * Get the display selling price based on whether conversion was requested
     */
    public Float getDisplaySellingPrice() {
        return pricesConverted != null && pricesConverted && sellingPriceConverted != null 
               ? sellingPriceConverted : sellingPrice;
    }
    
    /**
     * Get the display average purchase price based on whether conversion was requested
     */
    public Double getDisplayAveragePurchasePrice() {
        return pricesConverted != null && pricesConverted && averagePurchasePriceConverted != null 
               ? averagePurchasePriceConverted : averagePurchasePrice;
    }
    
    /**
     * Get the display total value based on whether conversion was requested
     */
    public Double getDisplayTotalValue() {
        return pricesConverted != null && pricesConverted && totalValueConverted != null 
               ? totalValueConverted : totalValue;
    }
    
    /**
     * Check if prices have been converted to a different currency
     */
    public boolean isPricesConverted() {
        return pricesConverted != null && pricesConverted;
    }
}
