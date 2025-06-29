package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTOResponse toResponse(Category category) {
        if (category == null) return null;

        return CategoryDTOResponse.builder()
                .id(category.getId())
                .name(category.getName()) // أو أي حقل يمثل الاسم
                .build();
    }

    // لو بدك تبني من DTO إلى Entity (اختياري)
    public Category toEntity(CategoryDTOResponse dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setId(dto.getId()); // فقط ID لو بدك تربطه بمنتج موجود
        return category;
    }

    // أو لو عندك CategoryRequestDTO، فيك تضيف toEntity(CategoryRequestDTO)
}
