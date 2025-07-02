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

        String translatedName = type.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(langCode))
                .map(TypeTranslation::getName)
                .findFirst()
                .orElse(type.getName());

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
