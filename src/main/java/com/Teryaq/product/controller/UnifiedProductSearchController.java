package com.Teryaq.product.controller;

import com.Teryaq.product.dto.UnifiedProductSearchDTO;
import com.Teryaq.product.service.UnifiedProductSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/search")
public class UnifiedProductSearchController {

    private final UnifiedProductSearchService unifiedProductSearchService;

    public UnifiedProductSearchController(UnifiedProductSearchService unifiedProductSearchService) {
        this.unifiedProductSearchService = unifiedProductSearchService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<UnifiedProductSearchDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "en") String languageCode) {
        
        List<UnifiedProductSearchDTO> results = unifiedProductSearchService.searchProducts(keyword, languageCode);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/all-products")
    public ResponseEntity<List<UnifiedProductSearchDTO>> getAllProducts(
            @RequestParam(defaultValue = "en") String languageCode) {
        
        List<UnifiedProductSearchDTO> results = unifiedProductSearchService.getAllProducts(languageCode);
        return ResponseEntity.ok(results);
    }
} 