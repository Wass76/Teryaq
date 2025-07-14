package com.Teryaq.product.aPharmacyProduct.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyProductTranslationDTOResponse {

    private String tradeName;
    private String scientificName;
    private String notes;
    private String languageName;
} 