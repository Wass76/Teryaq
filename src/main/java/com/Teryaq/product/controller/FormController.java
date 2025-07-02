package com.Teryaq.product.controller;


import com.Teryaq.product.dto.FormDTORequest;
import com.Teryaq.product.dto.FormDTOResponse;
import com.Teryaq.product.service.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/Forms")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    @GetMapping
    public List<FormDTOResponse> geForms(@RequestParam String lang) {
        return formService.getForms(lang);}

    @GetMapping("{id}")
    public FormDTOResponse getById(@PathVariable Long id,@RequestParam String lang) {
        return formService.getByID(id, lang);}

    @PostMapping
    public ResponseEntity<FormDTOResponse> createForm(@RequestBody FormDTORequest dto) {
        FormDTOResponse response = formService.insertForm(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    public FormDTOResponse updateForm(@PathVariable Long id, @RequestBody FormDTORequest Form) {
        return formService.editForm(id, Form);
    }

    @DeleteMapping("{id}")
    public void deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
    }

}
