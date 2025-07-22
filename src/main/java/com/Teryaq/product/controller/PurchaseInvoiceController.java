package com.Teryaq.product.controller;

import com.Teryaq.product.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.product.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.product.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.PurchaseInvoiceItem;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;

@RestController
@RequestMapping("/api/purchase-invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;

    @PostMapping
    public ResponseEntity<PurchaseInvoiceDTOResponse> create(@RequestBody PurchaseInvoiceDTORequest request) {
        return ResponseEntity.ok(purchaseInvoiceService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoiceDTOResponse> getById(@PathVariable Long id) {
        var invoice = purchaseInvoiceService.getInvoiceEntityById(id); // Add this method to return entity
        var pharmacyProducts = pharmacyProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> "PHARMACY".equals(i.getProductType()))
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        var masterProducts = masterProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> "MASTER".equals(i.getProductType()))
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        return ResponseEntity.ok(purchaseInvoiceService.getMapper().toResponse(invoice, pharmacyProducts, masterProducts));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseInvoiceDTOResponse>> listAll() {
        var invoices = purchaseInvoiceService.getAllInvoiceEntities(); // Add this method to return entities
        var responses = invoices.stream().map(invoice -> {
            var pharmacyProducts = pharmacyProductRepo.findAllById(
                invoice.getItems().stream()
                    .filter(i -> "PHARMACY".equals(i.getProductType()))
                    .map(PurchaseInvoiceItem::getProductId)
                    .toList()
            );
            var masterProducts = masterProductRepo.findAllById(
                invoice.getItems().stream()
                    .filter(i -> "MASTER".equals(i.getProductType()))
                    .map(PurchaseInvoiceItem::getProductId)
                    .toList()
            );
            return purchaseInvoiceService.getMapper().toResponse(invoice, pharmacyProducts, masterProducts);
        }).toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        purchaseInvoiceService.cancel(id);
        return ResponseEntity.ok().build();
    }
} 