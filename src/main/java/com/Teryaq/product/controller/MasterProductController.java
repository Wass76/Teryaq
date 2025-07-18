package com.Teryaq.product.controller;


import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.SearchDTORequest;
import com.Teryaq.product.service.MasterProductService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/master_products")
public class MasterProductController {

    private final MasterProductService masterProductService;

    public MasterProductController(MasterProductService masterProductService) {
        this.masterProductService = masterProductService;
    }

    @GetMapping
    public ResponseEntity<?> getAllMasterProducts(@RequestParam(name = "lang", defaultValue = "en") String lang ,
                                                  @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                                                  @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
                                                  @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
                                                  @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page , size , Sort.by(sortDirection,sortBy));
        return ResponseEntity.ok( masterProductService.getMasterProduct(lang , pageable));}

    @GetMapping("{id}")
    public  ResponseEntity<?> getMasterProductById(@PathVariable Long id,
                                                   @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(masterProductService.getByID(id, lang));
    }

    // @PostMapping("/search")
    // public ResponseEntity<?> searchProducts(@RequestBody SearchDTORequest requestDTO ,
    //                                         Pageable pageable) {
    //     return ResponseEntity.ok( masterProductService.search(requestDTO , pageable));
    // }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createMasterProduct(@RequestBody MProductDTORequest masterProduct,
                                                 @RequestParam(name = "lang", defaultValue = "en") String lang ) {
       return ResponseEntity.ok( masterProductService.insertMasterProduct(masterProduct, lang));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<?> updateMasterProductById(@PathVariable Long id,
                                                      @RequestBody MProductDTORequest masterProduct, 
                                                      @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(masterProductService.editMasterProduct(id, masterProduct, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public  ResponseEntity<Void> deleteMasterProductById(@PathVariable Long id) {
        masterProductService.deleteMasterProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}


