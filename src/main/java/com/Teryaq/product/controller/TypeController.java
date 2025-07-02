package com.Teryaq.product.controller;


import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.dto.TypeDTOResponse;
import com.Teryaq.product.service.TypeService;
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
    public List<TypeDTOResponse> getAll(@RequestParam String lang) {
        return typeService.getTypes(lang);
    }

    @GetMapping("{id}")
    public TypeDTOResponse getById(@PathVariable Long id,  @RequestParam String lang) {
        return typeService.getByID(id, lang);
    }


    @PostMapping
    public void createType(@RequestBody TypeDTORequest type) {
        typeService.insertType(type);
    }

    @PutMapping("{id}")
    public TypeDTOResponse updateType(@PathVariable Long id, @RequestBody TypeDTORequest type) {
        return typeService.editType(id, type);
    }

    @DeleteMapping("{id}")
    public void deleteType(@PathVariable Long id) {
        typeService.deleteType(id);
    }


}