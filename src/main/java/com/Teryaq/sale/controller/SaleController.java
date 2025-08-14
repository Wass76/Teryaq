package com.Teryaq.sale.controller;

import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sale Management", description = "APIs for managing sales and invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class SaleController {
    
    private final SaleService saleService;
   
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Create a new sale invoice", 
        description = "Creates a new sale invoice with the given request. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created sale invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid sale data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product or customer not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SaleInvoiceDTOResponse> createSale(
            @Parameter(description = "Sale invoice request data", required = true)
            @Valid @RequestBody SaleInvoiceDTORequest request) {
        SaleInvoiceDTOResponse response = saleService.createSaleInvoice(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get all sale invoices", 
        description = "Retrieves all sale invoices for the current pharmacy. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sale invoices",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<SaleInvoiceDTOResponse>> getAllSales() {
        List<SaleInvoiceDTOResponse> response = saleService.getAllSales();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get a sale invoice by ID", 
        description = "Retrieves a sale invoice by its ID. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sale invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Sale invoice not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SaleInvoiceDTOResponse> getSaleById(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id) {
        SaleInvoiceDTOResponse response = saleService.getSaleById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Cancel a sale invoice", 
        description = "Cancels a sale invoice and restores stock quantities. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully cancelled sale invoice"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Sale invoice not found"),
        @ApiResponse(responseCode = "409", description = "Sale invoice already cancelled"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id) {
        saleService.cancelSale(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search-by-date")
    public ResponseEntity<List<SaleInvoiceDTOResponse>> searchSaleInvoiceByDate(
        @RequestParam("date") LocalDate createdAt) {
        List<SaleInvoiceDTOResponse> response = saleService.searchSaleInvoiceByDate(createdAt);
        return ResponseEntity.ok(response);
    }
} 