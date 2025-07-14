package com.Teryaq.product.aPharmacyProduct.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyProductDTOResponse {

    private Long id;
    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private String notes;
    private float tax;
    private Set<String> barcodes;

    @Builder.Default
    private String productType = "PHARMACY";

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long pharmacyId;
    private String pharmacyName;

    private String type;
    private String form;
    private String manufacturer;
    private Set<String> categories;

    @Builder.Default
    private Set<PharmacyProductTranslationDTOResponse> translations = new HashSet<>();
}
