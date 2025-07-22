package com.Teryaq.product.controller;

import com.Teryaq.product.dto.PurchaseOrderDTORequest;
import com.Teryaq.product.dto.PurchaseOrderDTOResponse;
import com.Teryaq.product.service.PurchaseOrderService;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.PurchaseOrderItem;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;

    @PostMapping
    public ResponseEntity<PurchaseOrderDTOResponse> create(@RequestBody PurchaseOrderDTORequest request) {
        return ResponseEntity.ok(purchaseOrderService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTOResponse> getById(@PathVariable Long id) {
        var order = purchaseOrderService.getOrderEntityById(id); // Add this method to return entity
        var pharmacyProducts = pharmacyProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> "PHARMACY".equals(i.getProductType()))
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        var masterProducts = masterProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> "MASTER".equals(i.getProductType()))
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        return ResponseEntity.ok(purchaseOrderService.getMapper().toResponse(order, pharmacyProducts, masterProducts));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTOResponse>> listAll() {
        var orders = purchaseOrderService.getAllOrderEntities(); // Add this method to return entities
        var responses = orders.stream().map(order -> {
            var pharmacyProducts = pharmacyProductRepo.findAllById(
                order.getItems().stream()
                    .filter(i -> "PHARMACY".equals(i.getProductType()))
                    .map(PurchaseOrderItem::getProductId)
                    .toList()
            );
            var masterProducts = masterProductRepo.findAllById(
                order.getItems().stream()
                    .filter(i -> "MASTER".equals(i.getProductType()))
                    .map(PurchaseOrderItem::getProductId)
                    .toList()
            );
            return purchaseOrderService.getMapper().toResponse(order, pharmacyProducts, masterProducts);
        }).toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return ResponseEntity.ok().build();
    }
} 