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

@RestController
@RequestMapping("/api/purchase-invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {
    private final PurchaseInvoiceService purchaseInvoiceService;

    @PostMapping
    public ResponseEntity<PurchaseInvoiceDTOResponse> create(@Valid @RequestBody PurchaseInvoiceDTORequest request, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.create(request, language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoiceDTOResponse> getById(@Min(1) @PathVariable Long id, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.getById(id, language));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseInvoiceDTOResponse>> listAll(@RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAll(language));
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> listAllPaginated(
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAllPaginated(page, size, language));
    }
} 