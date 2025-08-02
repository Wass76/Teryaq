package com.Teryaq.sale.controller;

import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sale", description = "Sale API")
@RequiredArgsConstructor
public class SaleController {
    
    private final SaleService saleService;
   
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create a new sale invoice", description = "Creates a new sale invoice with the given request")
    @PostMapping
    public ResponseEntity<SaleInvoiceDTOResponse> createSale(@Valid @RequestBody SaleInvoiceDTORequest request) {
        SaleInvoiceDTOResponse response = saleService.createSaleInvoice(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get a sale invoice by ID", description = "Retrieves a sale invoice by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<SaleInvoiceDTOResponse> getSaleById(@Min(1) @PathVariable Long id) {
        SaleInvoiceDTOResponse response = saleService.getSaleById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Cancel a sale invoice", description = "Cancels a sale invoice and restores stock quantities")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(@Min(1) @PathVariable Long id) {
        saleService.cancelSale(id);
        return ResponseEntity.ok().build();
    }
} 