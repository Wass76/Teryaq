package com.Teryaq.product.controller;

import com.Teryaq.product.dto.ProductSearchDTO;
import com.Teryaq.product.service.ProductSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/search")
public class ProductSearchController {

    private final ProductSearchService ProductSearchService;

    public ProductSearchController(ProductSearchService ProductSearchService) {
        this.ProductSearchService = ProductSearchService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductSearchDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "en") String languageCode) {
        
        List<ProductSearchDTO> results = ProductSearchService.searchProducts(keyword, languageCode);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/all-products")
    public ResponseEntity<List<ProductSearchDTO>> getAllProducts(
            @RequestParam(defaultValue = "en") String languageCode) {
        
        List<ProductSearchDTO> results = ProductSearchService.getAllProducts(languageCode);
        return ResponseEntity.ok(results);
    }
} 