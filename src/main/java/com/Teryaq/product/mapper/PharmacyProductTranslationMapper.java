package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.PharmacyProductTranslationDTORequest;
import com.Teryaq.product.dto.PharmacyProductTranslationDTOResponse;
import com.Teryaq.product.entity.PharmacyProductTranslation;

import org.springframework.stereotype.Component;

@Component
public class PharmacyProductTranslationMapper {
    
    public PharmacyProductTranslationDTOResponse toResponse(PharmacyProductTranslation translation) {
        return PharmacyProductTranslationDTOResponse.builder()
                .tradeName(translation.getTradeName())
                .scientificName(translation.getScientificName())
                .languageName(translation.getLanguage().getName())
                .build();
    }
    
    public PharmacyProductTranslation toEntity(PharmacyProductTranslationDTORequest dto) {
        PharmacyProductTranslation translation = new PharmacyProductTranslation();
        translation.setTradeName(dto.getTradeName());
        translation.setScientificName(dto.getScientificName());
        return translation;
    }
} 