package com.Teryaq.product.controller;


import com.Teryaq.product.dto.ActiveIngredientDTORequest;
import com.Teryaq.product.dto.ActiveIngredientDTOResponse;
import com.Teryaq.product.entity.ActiveIngredient;
import com.Teryaq.product.service.ActiveIngredientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/activeIngredients")
public class ActiveIngredientController {

    private final ActiveIngredientService activeIngredientService;

    public ActiveIngredientController(ActiveIngredientService activeIngredientService) {
        this.activeIngredientService = activeIngredientService;
    }

    @GetMapping
    public List<ActiveIngredientDTOResponse> getActiveIngredients(@RequestParam String lang) {
        return activeIngredientService.getActiveIngredients(lang);}

    @GetMapping("{id}")
    public ActiveIngredientDTOResponse getById(@PathVariable Long id,@RequestParam String lang) {
        return activeIngredientService.getByID(id, lang);}

    @PostMapping
    public ResponseEntity<ActiveIngredientDTOResponse> createActiveIngredient(@RequestBody ActiveIngredientDTORequest dto) {
        ActiveIngredientDTOResponse response = activeIngredientService.insertActiveIngredient(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    public ActiveIngredientDTOResponse updateActiveIngredient(@PathVariable Long id, @RequestBody ActiveIngredientDTORequest activeIngredient) {
        return activeIngredientService.editActiveIngredient(id, activeIngredient);
    }

    @DeleteMapping("{id}")
    public void deleteActiveIngredient(@PathVariable Long id) {
        activeIngredientService.deleteActiveIngredient(id);
    }

}
