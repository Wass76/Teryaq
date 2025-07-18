package com.Teryaq.product.controller;


import com.Teryaq.product.dto.FormDTORequest;
import com.Teryaq.product.dto.FormDTOResponse;
import com.Teryaq.product.service.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/Forms")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {

        this.formService = formService;
    }

    @GetMapping 
    public  ResponseEntity<?> geForms(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(formService.getForms(lang));}

    @GetMapping("{id}")
    public  ResponseEntity<?> getById(@PathVariable Long id,
                                      @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(formService.getByID(id, lang));}

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createForm(@RequestBody FormDTORequest dto,
                                        @RequestParam(name = "lang", defaultValue = "en") String lang) {
        FormDTOResponse response = formService.insertForm(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);}

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<?> updateForm(@PathVariable Long id,
                                         @RequestBody FormDTORequest Form,
                                         @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(formService.editForm(id, Form, lang));
    }   

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
