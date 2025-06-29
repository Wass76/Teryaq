package com.Teryaq.product.controller;


import com.Teryaq.product.entity.Type;
import com.Teryaq.product.service.TypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/types")
public class TypeController {

    private final TypeService typeService;

    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @GetMapping
    public List<Type> getAll() {
        return typeService.getTypes();
    }

    @GetMapping("{id}")
    public Type getById(@PathVariable Long id) {
        return typeService.getByID(id);
    }


    @PostMapping
    public void createType(@RequestBody Type type) {
        typeService.insertType(type);
    }

    @PutMapping("{id}")
    public Type updateType(@PathVariable Long id, @RequestBody Type type) {
        return typeService.editType(id, type);
    }

    @DeleteMapping("{id}")
    public void deleteType(@PathVariable Long id) {
        typeService.deleteType(id);
    }


}