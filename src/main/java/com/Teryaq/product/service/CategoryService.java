package com.Teryaq.product.service;

import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.CategoryTranslation;
import com.Teryaq.product.mapper.CategoryMapper;
import com.Teryaq.product.repo.CategoryRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;
    private final LanguageRepo languageRepo;

    public CategoryService(CategoryRepo categoryRepo, CategoryMapper categoryMapper, LanguageRepo languageRepo) {
        this.categoryRepo = categoryRepo;
        this.categoryMapper = categoryMapper;
        this.languageRepo = languageRepo;
    }

    public List<CategoryDTOResponse> getCategories(String langCode) {
        return categoryRepo.findAll()
                .stream()
                .map(c -> categoryMapper.toResponse(c, langCode))
                .toList();
    }

    public CategoryDTOResponse getByID(long id, String langCode) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
        return categoryMapper.toResponse(category, langCode);
    }

    public CategoryDTOResponse insertCategory(CategoryDTORequest dto) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category = categoryRepo.save(category);

        CategoryTranslation translation = new CategoryTranslation();
        translation.setName(dto.getName());
        translation.setLanguage(languageRepo.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found")));
        translation.setCategory(category);
        category.getTranslations().add(translation);

        category = categoryRepo.save(category);

        return categoryMapper.toResponse(category, dto.getLanguageCode());
    }

    public CategoryDTOResponse editCategory(Long id, CategoryDTORequest dto) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));

        CategoryTranslation translation = category.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(dto.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Translation not found for this language"));

        translation.setName(dto.getName());
        return categoryMapper.toResponse(category, dto.getLanguageCode());
    }

    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepo.deleteById(id);
    }
}
