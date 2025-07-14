package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.TypeDTOResponse;
import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.entity.TypeTranslation;
import com.Teryaq.product.entity.Type;
import org.springframework.stereotype.Component;


@Component
public class TypeMapper {


    public TypeDTOResponse toResponse(Type type, String langCode) {
        if (type == null) return null;
    
        String sanitizedLangCode = langCode == null ? "en" : langCode.trim().toLowerCase();
        
        System.out.println("Processing type: " + type.getName() + " with langCode: " + sanitizedLangCode);
        System.out.println("Type translations count: " + (type.getTranslations() != null ? type.getTranslations().size() : 0));
        
        if (type.getTranslations() != null) {
            type.getTranslations().forEach(t -> {
                System.out.println("Translation: " + t.getName() + " for language: " + 
                    (t.getLanguage() != null ? t.getLanguage().getCode() : "null"));
            });
        }
    
        String translatedName = type.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
                .map(TypeTranslation::getName)
                .findFirst()
                .orElse(type.getName());
                
        System.out.println("Final translated name: " + translatedName);
        
        return TypeDTOResponse.builder()
                .id(type.getId())
                .name(translatedName)
                .build();
    }
    
    

    public Type toEntity(TypeDTOResponse dto) {
        if (dto == null) return null;

        Type type = new Type();
        type.setName(dto.getName());
        return type;
    }

    public Type toEntity(TypeDTORequest dto) {
        if (dto == null) return null;

        Type type = new Type();
        type.setName(dto.getName());
        return type;
    }
}
