package com.Teryaq.product.service;

import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.CategoryTranslation;
import com.Teryaq.product.mapper.CategoryMapper;
import com.Teryaq.product.repo.CategoryRepo;
import com.Teryaq.product.repo.CategoryTranslationRepo;
import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;
    private final LanguageRepo languageRepo;
    private final CategoryTranslationRepo categoryTranslationRepo;

    public CategoryService(CategoryRepo categoryRepo, CategoryMapper categoryMapper, 
                         LanguageRepo languageRepo, CategoryTranslationRepo categoryTranslationRepo) {
        this.categoryRepo = categoryRepo;
        this.categoryMapper = categoryMapper;
        this.languageRepo = languageRepo;
        this.categoryTranslationRepo = categoryTranslationRepo;
    }

    public List<CategoryDTOResponse> getCategories(String langCode) {
        log.info("Getting categories with langCode: {}", langCode);
        List<Category> categories = categoryRepo.findAllWithTranslations();
        log.info("Found {} categories", categories.size());
        
        return categories.stream()
                .map(category -> {
                    log.info("Processing category: {} with {} translations", category.getName(),
                            category.getTranslations() != null ? category.getTranslations().size() : 0);
                    return categoryMapper.toResponse(category, langCode);
                })
                .toList();
    }

    public CategoryDTOResponse getByID(long id, String langCode) {
        Category category = categoryRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
        return categoryMapper.toResponse(category, langCode);
    }

    public CategoryDTOResponse insertCategory(CategoryDTORequest dto, String langCode) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
        }
        
        Category category = new Category();
        category.setName(dto.getName());
        Category savedCategory = categoryRepo.save(category);

        List<CategoryTranslation> translations = dto.getTranslations().stream()
            .map(t -> {
                Language lang = languageRepo.findByCode(t.getLanguageCode())
                        .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                return new CategoryTranslation(t.getName(), savedCategory, lang);
            })
            .collect(Collectors.toList());

        categoryTranslationRepo.saveAll(translations);
        savedCategory.setTranslations(new HashSet<>(translations));

        return categoryMapper.toResponse(savedCategory, langCode);
    }

    public CategoryDTOResponse editCategory(Long id, CategoryDTORequest dto, String langCode) {
        return categoryRepo.findByIdWithTranslations(id).map(existing -> {
            if (!existing.getName().equals(dto.getName()) && categoryRepo.existsByName(dto.getName())) {
                throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
            }

            existing.setName(dto.getName());
            Category saved = categoryRepo.save(existing);

            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                categoryTranslationRepo.deleteByCategory(saved);

                List<CategoryTranslation> translations = dto.getTranslations().stream()
                        .map(t -> {
                            Language lang = languageRepo.findByCode(t.getLanguageCode())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                            return new CategoryTranslation(t.getName(), saved, lang);
                        })
                        .toList();

                categoryTranslationRepo.saveAll(translations);
            }

            // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
            Category updated = categoryRepo.findByIdWithTranslations(saved.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Updated category not found"));

            return categoryMapper.toResponse(updated, langCode);

        }).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepo.deleteById(id);
    }
}
