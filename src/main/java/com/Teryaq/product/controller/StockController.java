package com.Teryaq.product.controller;

import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockReportDTOResponse;
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
    public ResponseEntity<List<StockItemDTOResponse>> getStockByProductId(@PathVariable Long productId) {
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
    public ResponseEntity<Map<String, Object>> getTotalQuantityByProductId(@PathVariable Long productId) {
        Integer totalQuantity = stockManagementService.getTotalQuantityByProductId(productId);
        Map<String, Object> response = Map.of(
            "productId", productId,
            "totalQuantity", totalQuantity
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/product/{productId}/check-availability")
    @Operation(summary = "Check if quantity is available for a product", 
               description = "Check if a specific quantity is available for a given product ID")
    public ResponseEntity<Map<String, Object>> checkQuantityAvailability(
            @PathVariable Long productId, 
            @RequestParam Integer requiredQuantity) {
        boolean isAvailable = stockManagementService.isQuantityAvailable(productId, requiredQuantity);
        Map<String, Object> response = Map.of(
            "productId", productId,
            "requiredQuantity", requiredQuantity,
            "isAvailable", isAvailable,
            "availableQuantity", stockManagementService.getTotalQuantityByProductId(productId)
        );
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
            stockManagementService.getStockItemsByProductId(0L)); // This needs to be fixed
        
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
    public ResponseEntity<Map<String, Object>> getComprehensiveStockReport() {
        Map<String, Object> report = stockManagementService.getComprehensiveStockReport();
        return ResponseEntity.ok(report);
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
    public ResponseEntity<List<Map<String, Object>>> getAllStockItemsWithProductInfo() {
        List<Map<String, Object>> stockItems = stockManagementService.getAllStockItemsWithProductInfo();
        return ResponseEntity.ok(stockItems);
    }
} 