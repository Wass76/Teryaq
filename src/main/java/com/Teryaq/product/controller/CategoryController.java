package com.Teryaq.product.controller;


import com.Teryaq.product.dto.CategoryDTORequest;
import com.Teryaq.product.dto.CategoryDTOResponse;
import com.Teryaq.product.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {

        this.categoryService = categoryService;
    }

    @GetMapping
    public  ResponseEntity<?> getCategories(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(categoryService.getCategories(lang));}

    @GetMapping("{id}")
    public  ResponseEntity<?> getById(@PathVariable Long id, 
                                      @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(categoryService.getByID(id, lang));}

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTORequest dto,
                                            @RequestParam(name = "lang", defaultValue = "en") String lang) {
        CategoryDTOResponse response = categoryService.insertCategory(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<?> updateCategory(@PathVariable Long id,
                                             @RequestBody CategoryDTORequest category,
                                             @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(categoryService.editCategory(id, category, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
