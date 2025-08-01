package com.Teryaq.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharmacyProductDTORequest {

    @NotBlank(message = "Trade name is required")
    private String tradeName;

    private String scientificName;

    private String concentration;

    @NotBlank(message = "size is required")
    private String size;

    // @NotBlank(message = "Purchase Price is required")
    // private float refPurchasePrice;

    // @NotBlank(message = "Selling Price is required")
    // private float refSellingPrice;

    private String notes;
    private float tax;

    @NotNull(message = "At least one barcode is required")
    private Set<String> barcodes;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private Long formId;
    private Long manufacturerId;

    private Set<Long> categoryIds;

    private Set<PharmacyProductTranslationDTORequest> translations;
}
