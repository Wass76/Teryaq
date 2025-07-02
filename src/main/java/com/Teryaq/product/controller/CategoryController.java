package com.Teryaq.product.controller;


import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDTOResponse> getCategories(@RequestParam String lang) {
        return categoryService.getCategories(lang);}

    @GetMapping("{id}")
    public CategoryDTOResponse getById(@PathVariable Long id, @RequestParam String lang) {return categoryService.getByID(id, lang);}

    @PostMapping
    public ResponseEntity<CategoryDTOResponse> createCategory(@RequestBody CategoryDTORequest dto) {
        CategoryDTOResponse response = categoryService.insertCategory(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    public CategoryDTOResponse updateCategory(@PathVariable Long id, @RequestBody CategoryDTORequest category) {
        return categoryService.editCategory(id, category);
    }

    @DeleteMapping("{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

}
