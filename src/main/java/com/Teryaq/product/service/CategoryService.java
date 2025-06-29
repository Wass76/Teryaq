package com.Teryaq.product.service;


import com.Teryaq.product.entity.Category;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import com.Teryaq.product.repo.CategoryRepo;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<Category> getCategories() {return categoryRepo.findAll();}

    public Category getByID(long id) {return categoryRepo.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Category With Id" + id + "not found")) ;}

    public void insertCategory(Category category) {categoryRepo.save(category);}

    public Category editCategory(Long id,Category category) {

    return categoryRepo.findById(id).map(category1 ->{
    category1.setName(category.getName());
    return categoryRepo.save(category1);
            })
    .orElseThrow(()->new EntityNotFoundException("Category With Id" + id + "not found"));
    }

    public void deleteCategory(Long id) {
        if(!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found!") ;
        }
        categoryRepo.deleteById(id);}
}
