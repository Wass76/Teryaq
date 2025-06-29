package com.Teryaq.product.dto;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MProductDTORequest {

    private Long id;
    @NotBlank(message = "Trade name is required")
    private String tradeName;

    @NotBlank(message = "Scientific name is required")
    private String scientificName;

    @NotBlank(message = "Form is required")
    private String form;

    @NotBlank(message = "Concentration is required")
    private String concentration;

    @NotBlank(message = "size is required")
    private String size;

    @NotBlank(message = "manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "Purchase Price is required")
    private float refPurchasePrice;

    @NotBlank(message = "Selling Price is required")
    private float refSellingPrice;

    @NotBlank(message = "Active Ingredients is required")
    private String activeIngredients;

    private String notes;
    private float tax;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private Set<Long> categoryIds;

    private List<MProductTranslationDTORequest> translations;
}
