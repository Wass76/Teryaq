package com.Teryaq.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MProductDTOResponse {

    private Long id;
    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private String notes;
    private float tax;
    private String barcode;

    @Builder.Default
    private String productType = "MASTER";
    

    @Builder.Default
    private Boolean requiresPrescription = false;

    private String type;
    private String form;
    private String manufacturer;
    private Set<String> categories;

    // @Builder.Default
    // private Set<MProductTranslationDTOResponse> translations = new HashSet<>();
}
