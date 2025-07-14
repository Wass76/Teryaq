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
        
        System.out.println("Processing manufacturer: " + manufacturer.getName() + " with langCode: " + sanitizedLangCode);
        System.out.println("Manufacturer translations count: " + (manufacturer.getTranslations() != null ? manufacturer.getTranslations().size() : 0));
        
        if (manufacturer.getTranslations() != null) {
            manufacturer.getTranslations().forEach(t -> {
                System.out.println("Translation: " + t.getName() + " for language: " + 
                    (t.getLanguage() != null ? t.getLanguage().getCode() : "null"));
            });
        }

        String translatedName = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
                .map(ManufacturerTranslation::getName)
                .findFirst()
                .orElse(manufacturer.getName());
                
        System.out.println("Final translated name: " + translatedName);

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
