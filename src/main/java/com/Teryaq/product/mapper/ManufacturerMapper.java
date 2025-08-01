package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.entity.ManufacturerTranslation;
import com.Teryaq.product.entity.Manufacturer;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerMapper {

    public ManufacturerDTOResponse toResponse(Manufacturer manufacturer, String langCode) {
        if (manufacturer == null) return null;

        String sanitizedLangCode = langCode == null ? "en" : langCode.trim().toLowerCase();
        


        String translatedName = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
                .map(ManufacturerTranslation::getName)
                .findFirst()
                .orElse(manufacturer.getName());
                


        return ManufacturerDTOResponse.builder()
                .id(manufacturer.getId())
                .name(translatedName)
                .build();
    }

    public Manufacturer toEntity(ManufacturerDTORequest dto) {
        if (dto == null) return null;

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.getName());
        return manufacturer;
    }

}
