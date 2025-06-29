package com.Teryaq.product.controller;


import com.Teryaq.product.entity.Category;
import com.Teryaq.product.service.CategoryService;
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
    public List<Category> getCategories() {return categoryService.getCategories();}

    @GetMapping("{id}")
    public Category getById(@PathVariable Long id) {return categoryService.getByID(id);}

    @PostMapping
    public void createCategory(@RequestBody Category category) {
        categoryService.insertCategory(category);}

    @PutMapping("{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.editCategory(id, category);
    }

    @DeleteMapping("{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

}
