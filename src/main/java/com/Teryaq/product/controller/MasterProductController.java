package com.Teryaq.product.controller;


import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.service.MasterProductService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;


@RestController
@RequestMapping("api/v1/master_products")
@Tag(name = "Master Product Management", description = "APIs for managing master products")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class MasterProductController {

    private final MasterProductService masterProductService;

    public MasterProductController(MasterProductService masterProductService) {
        this.masterProductService = masterProductService;
    }

    @GetMapping
    @Operation(
        summary = "Get all master products",
        description = "Retrieves all master products with pagination and sorting support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved master products",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllMasterProducts(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "en") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page , size , Sort.by(sortDirection,sortBy));
        return ResponseEntity.ok(masterProductService.getMasterProduct(lang , pageable));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get master product by ID",
        description = "Retrieves a specific master product by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Language code", example = "en") 
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
    @Operation(
        summary = "Create new master product",
        description = "Creates a new master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid master product data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createMasterProduct(
            @Parameter(description = "Master product data", required = true)
            @Valid @RequestBody MProductDTORequest masterProduct,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "en") String lang) {
       return ResponseEntity.ok(masterProductService.insertMasterProduct(masterProduct, lang));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update master product",
        description = "Updates an existing master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid master product data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated master product data", required = true)
            @Valid @RequestBody MProductDTORequest masterProduct, 
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "en") String lang) {
        return ResponseEntity.ok(masterProductService.editMasterProduct(id, masterProduct, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete master product",
        description = "Deletes a master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted master product"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id) {
        masterProductService.deleteMasterProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}


