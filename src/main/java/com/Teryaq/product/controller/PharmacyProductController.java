package com.Teryaq.product.controller;

import com.Teryaq.product.dto.PharmacyProductDTORequest;
import com.Teryaq.product.service.PharmacyProductService;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/pharmacy_products")
public class PharmacyProductController {

    private final PharmacyProductService pharmacyProductService;

    public PharmacyProductController(PharmacyProductService pharmacyProductService) {
        this.pharmacyProductService = pharmacyProductService;
    }
    

        @GetMapping
    public ResponseEntity<?> getAllPharmacyProducts(@RequestParam(name = "lang", defaultValue = "en") String lang ,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
                                                  @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page , size , Sort.by(sortDirection,sortBy));
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProduct(lang, pageable));
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @GetMapping("pharmacy/{pharmacyId}")
    public ResponseEntity<?> getPharmacyProductsByPharmacyId(@PathVariable Long pharmacyId,
                                                           @RequestParam(name = "lang", defaultValue = "en") String lang,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                                           @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductByPharmacyId(pharmacyId, lang, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getPharmacyProductById(@PathVariable Long id,
                                                   @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(pharmacyProductService.getByID(id, lang));
    }

    @PostMapping
    public ResponseEntity<?> createPharmacyProduct(@Valid @RequestBody PharmacyProductDTORequest pharmacyProduct, 
                                                   @RequestParam(name = "lang", defaultValue = "en") String lang ) {
        return ResponseEntity.ok(pharmacyProductService.insertPharmacyProduct(pharmacyProduct, lang));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updatePharmacyProductById(@PathVariable Long id,
                                                      @RequestBody PharmacyProductDTORequest pharmacyProduct, 
                                                      @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(pharmacyProductService.editPharmacyProduct(id, pharmacyProduct, lang));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePharmacyProductById(@PathVariable Long id) {
        pharmacyProductService.deletePharmacyProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
