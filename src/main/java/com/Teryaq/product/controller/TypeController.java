package com.Teryaq.product.controller;


import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.service.TypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/types")
public class TypeController {

    private final TypeService typeService;

    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @GetMapping
    public  ResponseEntity<?> getAll(@RequestParam(name = "lang", defaultValue = "en") String lang) {
        System.out.println("Received langCode: " + lang);
        return ResponseEntity.ok(typeService.getTypes(lang));
    }

    @GetMapping("{id}")
    public  ResponseEntity<?> getById(@PathVariable Long id,  @RequestParam String lang) {
        return ResponseEntity.ok(typeService.getByID(id, lang));
    }

    
    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createType(@RequestBody TypeDTORequest type,
                                        @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(typeService.insertType(type, lang));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<?> updateType(@PathVariable Long id,
                                         @RequestBody TypeDTORequest type,
                                         @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(typeService.editType(id, type, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<Void> deleteType(@PathVariable Long id) {
        typeService.deleteType(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}