package com.Teryaq.purchase.controller;

import com.Teryaq.purchase.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.product.dto.PaginationDTO;
import com.Teryaq.purchase.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/purchase-invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoice Management", description = "APIs for managing purchase invoices")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PurchaseInvoiceController {
    private final PurchaseInvoiceService purchaseInvoiceService;

    @PostMapping
    @Operation(
        summary = "Create new purchase invoice",
        description = "Creates a new purchase invoice with the given request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created purchase invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase invoice data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseInvoiceDTOResponse> create(
            @Parameter(description = "Purchase invoice data", required = true)
            @Valid @RequestBody PurchaseInvoiceDTORequest request, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.create(request, language));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get purchase invoice by ID",
        description = "Retrieves a specific purchase invoice by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved purchase invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Purchase invoice not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseInvoiceDTOResponse> getById(
            @Parameter(description = "Purchase invoice ID", example = "1") 
            @Min(1) @PathVariable Long id, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.getById(id, language));
    }

    @GetMapping
    @Operation(
        summary = "Get all purchase invoices",
        description = "Retrieves all purchase invoices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all purchase invoices",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PurchaseInvoiceDTOResponse>> listAll(
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAll(language));
    }

    @GetMapping("/paginated")
    @Operation(
        summary = "Get paginated purchase invoices",
        description = "Retrieves purchase invoices with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase invoices",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> listAllPaginated(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10") 
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAllPaginated(page, size, language));
    }
} 