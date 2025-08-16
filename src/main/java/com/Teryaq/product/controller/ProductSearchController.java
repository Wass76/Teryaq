package com.Teryaq.product.controller;

import com.Teryaq.product.dto.ProductSearchDTOResponse;
import com.Teryaq.product.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductSearchDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products")
    @Operation(summary = "Search products by keyword", description = "Search for products using a keyword in both master and pharmacy products")
    public ResponseEntity<Page<ProductSearchDTOResponse>> searchProducts(
            @Parameter(description = "Search keyword", example = "باراسيتامول") 
            @RequestParam String keyword,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "tradeName") 
            @RequestParam(defaultValue = "tradeName") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "asc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductSearchDTOResponse> results = ProductSearchService.searchProducts(keyword, lang, pageable);
        return ResponseEntity.ok(results);
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductSearchDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all-products")
    @Operation(summary = "Get all products", description = "Get all products from both master and pharmacy products")
    public ResponseEntity<Page<ProductSearchDTOResponse>> getAllProducts(
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "tradeName") 
            @RequestParam(defaultValue = "tradeName") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "asc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ProductSearchDTOResponse> results = ProductSearchService.getAllProducts(lang, pageable);
        return ResponseEntity.ok(results);
    }
} 