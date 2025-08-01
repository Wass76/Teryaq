package com.Teryaq.sale.controller;

import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.service.SaleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sale", description = "Sale API")
public class SaleController {
    @Autowired
    private SaleService saleService;
   
    @Operation(summary = "Create a new sale invoice", description = "Creates a new sale invoice with the given request")
    @PostMapping
    public ResponseEntity<SaleInvoiceDTOResponse> createSale(@RequestBody SaleInvoiceDTORequest request) {
        // validate the request
        if (request.getCustomerId() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        if (request.getItems() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        SaleInvoiceDTOResponse response = saleService.createSaleInvoice(request);
        return ResponseEntity.ok(response);
    }
} 