package com.Teryaq.product.service;

import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.mapper.CategoryMapper;
import com.Teryaq.product.repo.CategoryRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepo categoryRepo, CategoryMapper categoryMapper) {
        this.categoryRepo = categoryRepo;
        this.categoryMapper = categoryMapper;
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

    public CategoryDTOResponse insertCategory(CategoryDTORequest dto, String langCode) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Category already exists");
        }
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepo.save(category);
        return categoryMapper.toResponse(saved, langCode);
    }

    public CategoryDTOResponse editCategory(Long id, CategoryDTORequest dto, String langCode) {
        return categoryRepo.findById(id).map(existing -> {
            Category updated = categoryMapper.toEntity(dto);
            updated.setId(existing.getId());
            Category saved = categoryRepo.save(updated);
            return categoryMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepo.deleteById(id);
    }
}
