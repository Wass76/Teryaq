package com.Teryaq.product.controller;

import com.Teryaq.product.dto.ProductSearchDTO;
import com.Teryaq.product.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("api/v1/search")
@CrossOrigin(origins = "*")
@Tag(name = "Product Search", description = "APIs for searching products")
public class ProductSearchController {

    private static final Logger logger = LoggerFactory.getLogger(ProductSearchController.class);
    private final ProductSearchService ProductSearchService;

    public ProductSearchController(ProductSearchService ProductSearchService) {
        this.ProductSearchService = ProductSearchService;
        logger.info("ProductSearchController initialized successfully");
    }

    @GetMapping("/products")
    @Operation(summary = "Search products by keyword", description = "Search for products using a keyword in both master and pharmacy products")
    public ResponseEntity<List<ProductSearchDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "ar") String lang) {
        
        List<ProductSearchDTO> results = ProductSearchService.searchProducts(keyword, lang);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/all-products")
    @Operation(summary = "Get all products", description = "Get all products from both master and pharmacy products")
    public ResponseEntity<List<ProductSearchDTO>> getAllProducts(
            @RequestParam(defaultValue = "ar") String lang) {
        
        List<ProductSearchDTO> results = ProductSearchService.getAllProducts(lang);
        return ResponseEntity.ok(results);
    }
} 