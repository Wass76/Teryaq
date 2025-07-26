package com.Teryaq.product.controller;

import com.Teryaq.product.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.product.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.product.dto.PaginationDTO;
import com.Teryaq.product.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {
    private final PurchaseInvoiceService purchaseInvoiceService;

    @PostMapping
    public ResponseEntity<PurchaseInvoiceDTOResponse> create(@RequestBody PurchaseInvoiceDTORequest request, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.create(request, language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoiceDTOResponse> getById(@PathVariable Long id, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.getById(id, language));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseInvoiceDTOResponse>> listAll(@RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAll(language));
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> listAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAllPaginated(page, size, language));
    }
} 