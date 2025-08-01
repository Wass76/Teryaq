package com.Teryaq.product.controller;

import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockReportDTOResponse;
import com.Teryaq.product.dto.StockQuantityDTOResponse;
import com.Teryaq.product.dto.StockAvailabilityDTOResponse;
import com.Teryaq.product.dto.ComprehensiveStockReportDTOResponse;
import com.Teryaq.product.dto.StockItemWithProductInfoDTOResponse;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.service.StockManagementService;
import com.Teryaq.product.mapper.StockItemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import com.Teryaq.product.entity.StockItem;

@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock Management", description = "APIs for managing inventory stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockManagementService stockManagementService;
    private final StockItemMapper stockItemMapper;
    
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock items for a specific product", 
               description = "Retrieve all stock items for a given product ID")
    public ResponseEntity<List<StockItemDTOResponse>> getStockByProductId(@Min(1) @PathVariable Long productId) {
        List<StockItemDTOResponse> stockItems = stockItemMapper.toResponseList(
            stockManagementService.getStockItemsByProductId(productId));
        return ResponseEntity.ok(stockItems);
    }
    
    @GetMapping("/product/{productId}/available")
    @Operation(summary = "Get available stock for a specific product", 
               description = "Retrieve available stock items (quantity > 0 and not expired) for a given product ID")
    public ResponseEntity<List<StockItemDTOResponse>> getAvailableStockByProductId(@PathVariable Long productId) {
        List<StockItemDTOResponse> stockItems = stockItemMapper.toResponseList(
            stockManagementService.getAvailableStockByProductId(productId));
        return ResponseEntity.ok(stockItems);
    }
    
    @GetMapping("/product/{productId}/quantity")
    @Operation(summary = "Get total quantity for a specific product", 
               description = "Get the total available quantity for a given product ID")
    public ResponseEntity<StockQuantityDTOResponse> getTotalQuantityByProductId(@PathVariable Long productId) {
        Integer totalQuantity = stockManagementService.getTotalQuantityByProductId(productId);
        StockQuantityDTOResponse response = StockQuantityDTOResponse.builder()
                .productId(productId)
                .totalQuantity(totalQuantity)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/product/{productId}/check-availability")
    @Operation(summary = "Check if quantity is available for a product", 
               description = "Check if a specific quantity is available for a given product ID")
    public ResponseEntity<StockAvailabilityDTOResponse> checkQuantityAvailability(
            @Min(1) @PathVariable Long productId, 
            @Min(1) @Max(10000) @RequestParam Integer requiredQuantity) {
        boolean isAvailable = stockManagementService.isQuantityAvailable(productId, requiredQuantity);
        Integer availableQuantity = stockManagementService.getTotalQuantityByProductId(productId);
        StockAvailabilityDTOResponse response = StockAvailabilityDTOResponse.builder()
                .productId(productId)
                .requiredQuantity(requiredQuantity)
                .isAvailable(isAvailable)
                .availableQuantity(availableQuantity)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired items", 
               description = "Retrieve all stock items that have expired")
    public ResponseEntity<List<StockItemDTOResponse>> getExpiredItems() {
        List<StockItemDTOResponse> expiredItems = stockItemMapper.toResponseList(
            stockManagementService.getExpiredItems());
        return ResponseEntity.ok(expiredItems);
    }
    
    @GetMapping("/expiring-soon")
    @Operation(summary = "Get items expiring soon", 
               description = "Retrieve all stock items that will expire within 30 days")
    public ResponseEntity<List<StockItemDTOResponse>> getItemsExpiringSoon() {
        List<StockItemDTOResponse> expiringItems = stockItemMapper.toResponseList(
            stockManagementService.getItemsExpiringSoon());
        return ResponseEntity.ok(expiringItems);
    }
    
    @GetMapping("/report/{productType}")
    @Operation(summary = "Get stock report by product type", 
               description = "Get comprehensive stock report for a specific product type")
    public ResponseEntity<StockReportDTOResponse> getStockReportByProductType(
            @PathVariable ProductType productType) {
        Map<String, Object> reportData = stockManagementService.getStockReportByProductType(productType);
        List<StockItemDTOResponse> stockItems = stockItemMapper.toResponseList(
            stockManagementService.getAllStockItems()); // Fixed: Get all stock items for the pharmacy
        
        StockReportDTOResponse report = StockReportDTOResponse.builder()
                .productType(productType)
                .totalItems((Integer) reportData.get("totalItems"))
                .totalQuantity((Integer) reportData.get("totalQuantity"))
                .totalValue((Double) reportData.get("totalValue"))
                .expiredItems((Long) reportData.get("expiredItems"))
                .expiringSoonItems((Long) reportData.get("expiringSoonItems"))
                .stockItems(stockItems)
                .build();
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/comprehensive-report")
    @Operation(summary = "Get comprehensive stock report", 
               description = "Get a comprehensive stock report including all product types and alerts")
    public ResponseEntity<ComprehensiveStockReportDTOResponse> getComprehensiveStockReport() {
        Map<String, Object> report = stockManagementService.getComprehensiveStockReport();
        List<StockItemDTOResponse> expiredItems = stockItemMapper.toResponseList(
            stockManagementService.getExpiredItems());
        List<StockItemDTOResponse> expiringSoonItems = stockItemMapper.toResponseList(
            stockManagementService.getItemsExpiringSoon());
        
        ComprehensiveStockReportDTOResponse response = ComprehensiveStockReportDTOResponse.builder()
                .pharmacyProducts((Map<String, Object>) report.get("pharmacyProducts"))
                .masterProducts((Map<String, Object>) report.get("masterProducts"))
                .expiredItems(expiredItems)
                .expiringSoonItems(expiringSoonItems)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all stock items", 
               description = "Retrieve all stock items in the inventory")
    public ResponseEntity<List<StockItemDTOResponse>> getAllStockItems() {
        List<StockItemDTOResponse> stockItems = stockItemMapper.toResponseList(
            stockManagementService.getAllStockItems());
        return ResponseEntity.ok(stockItems);
    }

    @GetMapping("/all-with-product-info")
    @Operation(summary = "Get all stock items with product information", 
               description = "Retrieve all stock items with detailed product information")
    public ResponseEntity<List<StockItemWithProductInfoDTOResponse>> getAllStockItemsWithProductInfo() {
        List<StockItem> stockItems = stockManagementService.getAllStockItems();
        List<StockItemWithProductInfoDTOResponse> response = stockItems.stream()
            .map(item -> StockItemWithProductInfoDTOResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productType(item.getProductType())
                .quantity(item.getQuantity())
                .actualPurchasePrice(item.getActualPurchasePrice())
                .expiryDate(item.getExpiryDate())
                .dateAdded(item.getDateAdded())
                .productName(stockManagementService.getProductName(item.getProductId(), item.getProductType()))
                .batchNo(item.getBatchNo())
                .bonusQty(item.getBonusQty())
                .addedBy(item.getAddedBy())
                .purchaseInvoiceId(item.getPurchaseInvoice() != null ? item.getPurchaseInvoice().getId() : null)
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
} 