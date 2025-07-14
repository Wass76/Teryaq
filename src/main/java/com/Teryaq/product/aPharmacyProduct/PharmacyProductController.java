package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductDTORequest;
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
    private final PharmacyProductMapper pharmacyProductMapper;

    public PharmacyProductController(PharmacyProductService pharmacyProductService, PharmacyProductMapper pharmacyProductMapper) {
        this.pharmacyProductService = pharmacyProductService;
        this.pharmacyProductMapper = pharmacyProductMapper;
    }

    @GetMapping
    public ResponseEntity<?> getAllPharmacyProducts(@RequestParam String lang ,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
                                                  @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page , size , Sort.by(sortDirection,sortBy));
        var products = pharmacyProductService.getPharmacyProduct(pageable)
            .map(pharmacyProductMapper::toListDTO);
        return ResponseEntity.ok(products);
    }

    @GetMapping("pharmacy/{pharmacyId}")
    public ResponseEntity<?> getPharmacyProductsByPharmacyId(@PathVariable Long pharmacyId,
                                                           @RequestParam String lang,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                                           @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        var products = pharmacyProductService.getPharmacyProductByPharmacyId(pharmacyId, lang, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("pharmacy/{pharmacyId}/translated")
    public ResponseEntity<?> getPharmacyProductsByPharmacyIdWithTranslation(@PathVariable Long pharmacyId,
                                                                          @RequestParam String lang,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                          @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        var products = pharmacyProductService.getPharmacyProductByPharmacyIdWithTranslation(pharmacyId, lang, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("{id}")
    public  ResponseEntity<?> getPharmacyProductById(@PathVariable Long id, @RequestParam String lang) {
        return ResponseEntity.ok(pharmacyProductService.getByID(id, lang));
    }

    @PostMapping
    //@PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> createPharmacyProduct(@RequestBody PharmacyProductDTORequest pharmacyProduct, @RequestParam String lang ) {
       return ResponseEntity.ok( pharmacyProductService.insertPharmacyProduct(pharmacyProduct, lang));
    }

    @PutMapping("{id}")
    //@PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public  ResponseEntity<?> updatePharmacyProductById(@PathVariable Long id,
                                                 @RequestBody PharmacyProductDTORequest pharmacyProduct, @RequestParam String lang) {
        return ResponseEntity.ok(pharmacyProductService.editPharmacyProduct(id, pharmacyProduct, lang));
    }

    @DeleteMapping("{id}")
    //@PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public  ResponseEntity<Void> deletePharmacyProductById(@PathVariable Long id) {
        pharmacyProductService.deletePharmacyProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
