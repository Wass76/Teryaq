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
public class ProductSearchDTOResponse {
    private Long id;
    private String tradeName;
    private String scientificName;
    private Set<String> barcodes;
   
    private String productTypeName;
    private Boolean requiresPrescription;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    
    private Long pharmacyId;
    private String pharmacyName;
    
    private Long typeId;
    private String type;

    private Long formId;
    private String form;   

    private Long manufacturerId;
    private String manufacturer;

    private Set<Long> categoryIds;
    private Set<String> categories;

    private String notes;
    private float tax;
    private Integer quantity; 
} 