package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.CategoryTranslation;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTOResponse toResponse(Category category, String langCode) {
        if (category == null) return null;

        String translatedName = category.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(langCode))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());

        return CategoryDTOResponse.builder()
                .id(category.getId())
                .name(translatedName)
                .build();
    }

    public Category toEntity(CategoryDTOResponse dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

//    public Category toEntity(CategoryDTORequest dto) {
//        if (dto == null) return null;
//
//        Category category = new Category();
//        category.setName(dto.getName());
//        return category;
//    }

}
