package com.Teryaq.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchDTO {
    private Long id;
    private String tradeName;
    private String scientificName;
    private String barcode;
    private String productType; // "MASTER" or "PHARMACY"
    private Boolean requiresPrescription;
    private String concentration;
    private String size;
    // private float refPurchasePrice;
    // private float refSellingPrice;
    
    private Long pharmacyId;
    private String pharmacyName;
    

    private String type;
    private String form;
    private String manufacturer;
    private String notes;
    private float tax;
} 