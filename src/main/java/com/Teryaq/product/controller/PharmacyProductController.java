package com.Teryaq.product.controller;

import com.Teryaq.product.dto.PharmacyProductDTORequest;
import com.Teryaq.product.mapper.PharmacyProductMapper;
import com.Teryaq.product.service.PharmacyProductService;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.service.UserService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/pharmacy_products")
public class PharmacyProductController {

    private final PharmacyProductService pharmacyProductService;
    private final PharmacyProductMapper pharmacyProductMapper;
    private final UserService userService;

    public PharmacyProductController(PharmacyProductService pharmacyProductService, PharmacyProductMapper pharmacyProductMapper, UserService userService) {
        this.pharmacyProductService = pharmacyProductService;
        this.pharmacyProductMapper = pharmacyProductMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllPharmacyProducts(@RequestParam(name = "lang", defaultValue = "en") String lang ,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
                                                  @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page , size , Sort.by(sortDirection,sortBy));
        var products = pharmacyProductService.getPharmacyProduct(pageable)
            .map(product -> pharmacyProductMapper.toListDTO(product, lang));
        return ResponseEntity.ok(products);
    }

    @GetMapping("pharmacy/{pharmacyId}")
    public ResponseEntity<?> getPharmacyProductsByPharmacyId(@PathVariable Long pharmacyId,
                                                           @RequestParam(name = "lang", defaultValue = "en") String lang,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                                           @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        var products = pharmacyProductService.getPharmacyProductByPharmacyId(pharmacyId, lang, pageable);
        return ResponseEntity.ok(products);
    }

    // @GetMapping("pharmacy/{pharmacyId}/translated")
    // public ResponseEntity<?> getPharmacyProductsByPharmacyIdWithTranslation(@PathVariable Long pharmacyId,
    //                                                                       @RequestParam String lang,
    //                                                                       @RequestParam(defaultValue = "0") int page,
    //                                                                       @RequestParam(defaultValue = "10") int size,
    //                                                                       @RequestParam(defaultValue = "createdAt") String sortBy,
    //                                                                       @RequestParam(defaultValue = "desc") String direction) {
    //     Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
    //     Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    //     var products = pharmacyProductService.getPharmacyProductByPharmacyIdWithTranslation(pharmacyId, lang, pageable);
    //     return ResponseEntity.ok(products);
    // }

    @GetMapping("{id}")
    public  ResponseEntity<?> getPharmacyProductById(@PathVariable Long id,
                                                     @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(pharmacyProductService.getByID(id, lang));
    }

    @PostMapping
    public ResponseEntity<?> createPharmacyProduct(@RequestBody PharmacyProductDTORequest pharmacyProduct, 
                                                   @RequestParam(name = "lang", defaultValue = "en") String lang ) {

        User currentUser = userService.getCurrentUser();
        
        // التحقق من أن المستخدم هو Employee
        if (!(currentUser instanceof Employee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only pharmacy employees can create pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        
        // التحقق من أن الموظف مرتبط بصيدلية
        if (employee.getPharmacy() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        // التحقق من أن pharmacyId ليس null
        if (pharmacyId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Pharmacy ID is null for employee");
        }
                                                                                       
        return ResponseEntity.ok(pharmacyProductService.insertPharmacyProduct(pharmacyProduct, lang, pharmacyId));
    }

    @PutMapping("{id}")
    public  ResponseEntity<?> updatePharmacyProductById(@PathVariable Long id,
                                                        @RequestBody PharmacyProductDTORequest pharmacyProduct, 
                                                        @RequestParam(name = "lang", defaultValue = "en") String lang) {
        User currentUser = userService.getCurrentUser();
        
        // التحقق من أن المستخدم هو Employee
        if (!(currentUser instanceof Employee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only pharmacy employees can update pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        
        // التحقق من أن الموظف مرتبط بصيدلية
        if (employee.getPharmacy() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        // التحقق من أن pharmacyId ليس null
        if (pharmacyId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Pharmacy ID is null for employee");
        }
        
        return ResponseEntity.ok(pharmacyProductService.editPharmacyProduct(id, pharmacyProduct, lang, pharmacyId));
    }

    @DeleteMapping("{id}")
    public  ResponseEntity<Void> deletePharmacyProductById(@PathVariable Long id) {
        pharmacyProductService.deletePharmacyProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
