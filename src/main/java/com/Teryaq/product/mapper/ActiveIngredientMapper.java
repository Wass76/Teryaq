package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.ActiveIngredientDTORequest;
import com.Teryaq.product.dto.ActiveIngredientDTOResponse;
import com.Teryaq.product.entity.ActiveIngredient;
import com.Teryaq.product.entity.ActiveIngredientTranslation;
import org.springframework.stereotype.Component;

@Component
public class ActiveIngredientMapper {

    public ActiveIngredientDTOResponse toResponse(ActiveIngredient activeIngredient, String langCode) {
        if (activeIngredient == null) return null;

        String translatedName = activeIngredient.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(langCode))
                .map(ActiveIngredientTranslation::getName)
                .findFirst()
                .orElse(activeIngredient.getName());

        return ActiveIngredientDTOResponse.builder()
                .id(activeIngredient.getId())
                .name(translatedName)
                .build();
    }

    public ActiveIngredient toEntity(ActiveIngredientDTOResponse dto) {
        if (dto == null) return null;

        ActiveIngredient activeIngredient = new ActiveIngredient();
        activeIngredient.setName(dto.getName());
        return activeIngredient;
    }

    public ActiveIngredient toEntity(ActiveIngredientDTORequest dto) {
        if (dto == null) return null;

        ActiveIngredient activeIngredient = new ActiveIngredient();
        activeIngredient.setName(dto.getName());
        return activeIngredient;
    }

}
