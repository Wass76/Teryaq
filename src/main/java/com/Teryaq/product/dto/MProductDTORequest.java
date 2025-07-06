package com.Teryaq.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MProductDTORequest {


    @NotBlank(message = "Trade name is required")
    private String tradeName;

    @NotBlank(message = "Scientific name is required")
    private String scientificName;

    @NotBlank(message = "Concentration is required")
    private String concentration;

    @NotBlank(message = "size is required")
    private String size;

    @NotBlank(message = "Purchase Price is required")
    private float refPurchasePrice;

    @NotBlank(message = "Selling Price is required")
    private float refSellingPrice;

    private String notes;
    private float tax;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private Long formId;
    private Long manufacturerId;

    private Set<Long> categoryIds;

    private Set<Long> activeIngredientIds;

    private Set<MProductTranslationDTORequest> translations;
}
