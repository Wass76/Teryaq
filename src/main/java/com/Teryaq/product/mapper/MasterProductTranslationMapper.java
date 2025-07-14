package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.MProductTranslationDTORequest;
import com.Teryaq.product.dto.MProductTranslationDTOResponse;
import com.Teryaq.product.entity.MasterProductTranslation;
import org.springframework.stereotype.Component;

@Component
public class MasterProductTranslationMapper {
    
    public MProductTranslationDTOResponse toResponse(MasterProductTranslation translation) {
        return MProductTranslationDTOResponse.builder()
                .tradeName(translation.getTradeName())
                .scientificName(translation.getScientificName())
                .notes(translation.getNotes())
                .languageName(translation.getLanguage().getName())
                .build();
    }
    
    public MasterProductTranslation toEntity(MProductTranslationDTORequest dto) {
        MasterProductTranslation translation = new MasterProductTranslation();
        translation.setTradeName(dto.getTradeName());
        translation.setScientificName(dto.getScientificName());
        translation.setNotes(dto.getNotes());
        return translation;
    }
}
