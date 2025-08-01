package com.Teryaq.product.dto;


import java.util.Set;


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
    private Set<String> barcodes;
   
    private String productTypeName;
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