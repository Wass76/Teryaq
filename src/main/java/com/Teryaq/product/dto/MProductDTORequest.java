package com.Teryaq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import jakarta.persistence.Column;
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

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Barcode is required")
    private String barcode;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private Long formId;
    private Long manufacturerId;

    @Schema(description = "List of category IDs")
    private List<Long> categoryIds;

    @Schema(description = "List of category IDs")
    private List<Long> activeIngredientIds;

    private List<MProductTranslationDTORequest> translations;
}
