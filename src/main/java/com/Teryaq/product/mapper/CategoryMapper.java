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

        String sanitizedLangCode = langCode == null ? "en" : langCode.trim().toLowerCase();
        
        System.out.println("Processing category: " + category.getName() + " with langCode: " + sanitizedLangCode);
        System.out.println("Category translations count: " + (category.getTranslations() != null ? category.getTranslations().size() : 0));
        
        if (category.getTranslations() != null) {
            category.getTranslations().forEach(t -> {
                System.out.println("Translation: " + t.getName() + " for language: " + 
                    (t.getLanguage() != null ? t.getLanguage().getCode() : "null"));
            });
        }

        String translatedName = category.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());
                
        System.out.println("Final translated name: " + translatedName);

        return CategoryDTOResponse.builder()
                .id(category.getId())
                .name(translatedName)
                .build();
    }


    public Category toEntity(CategoryDTORequest dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

}
