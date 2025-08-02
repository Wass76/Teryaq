package com.Teryaq.purchase.controller;

import com.Teryaq.purchase.dto.PurchaseOrderDTORequest;
import com.Teryaq.purchase.dto.PurchaseOrderDTOResponse;
import com.Teryaq.product.dto.PaginationDTO;
import com.Teryaq.purchase.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.Teryaq.product.Enum.OrderStatus;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderDTOResponse> create(@RequestBody PurchaseOrderDTORequest request, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.create(request, language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTOResponse> getById(@PathVariable Long id, @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getById(id, language));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTOResponse>> listAll(@RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.listAll(language));
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> listAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.listAllPaginated(page, size, language));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderDTOResponse>> getByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getByStatus(status, language));
    }

    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> getByStatusPaginated(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getByStatusPaginated(status, page, size, language));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return ResponseEntity.ok().build();
    }
} 