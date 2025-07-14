package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductTranslationDTORequest;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductTranslationDTOResponse;

import org.springframework.stereotype.Component;

@Component
public class PharmacyProductTranslationMapper {
    
    public PharmacyProductTranslationDTOResponse toResponse(PharmacyProductTranslation translation) {
        return PharmacyProductTranslationDTOResponse.builder()
                .tradeName(translation.getTradeName())
                .scientificName(translation.getScientificName())
                .notes(translation.getNotes())
                .languageName(translation.getLanguage().getName())
                .build();
    }
    
    public PharmacyProductTranslation toEntity(PharmacyProductTranslationDTORequest dto) {
        PharmacyProductTranslation translation = new PharmacyProductTranslation();
        translation.setTradeName(dto.getTradeName());
        translation.setScientificName(dto.getScientificName());
        translation.setNotes(dto.getNotes());
        return translation;
    }
} 