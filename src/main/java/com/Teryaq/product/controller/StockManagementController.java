package com.Teryaq.product.controller;

import com.Teryaq.product.dto.StockItemEditRequest;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.service.StockService;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockItemDetailDTOResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock Managment", description = "Managing Stock on the pharmacy")
public class StockManagementController {

    private final StockService stockService;
    
    @Transactional
    @PutMapping("/{stockItemId}/edit")
    @Operation(summary = "edit stock quantity and expiry date", description = "edit stock quantity and expiry date together")
    public ResponseEntity<StockItemDTOResponse> adjustStockQuantityAndExpiryDate(
            @PathVariable Long stockItemId,
            @Valid @RequestBody StockItemEditRequest request) {
        
        StockItemDTOResponse result = stockService.editStockQuantityAndExpiryDate(
            stockItemId,
            request.getQuantity(),
            request.getExpiryDate(),
            request.getMinStockLevel(),
            request.getReasonCode(),
            request.getAdditionalNotes()
        );
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search for Stock Item", description = "Search by Invoice Number")
    public ResponseEntity<List<StockItemDTOResponse>> advancedStockSearch(
            @RequestParam(required = false) String keyword) {
        List<StockItemDTOResponse> stockItems = stockService.stockItemSearch(keyword);
        return ResponseEntity.ok(stockItems);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "expired products", description = "get all expired products")
    public ResponseEntity<List<StockItem>> getExpiredItems() {
        List<StockItem> stockItems = stockService.getExpiredItems();
        return ResponseEntity.ok(stockItems);
    }
    
    @GetMapping("/expiring-soon")
    @Operation(summary = "expiring-soon products", description = "get all expiring-soon products during 30 days")
    public ResponseEntity<List<StockItem>> getItemsExpiringSoon() {
        List<StockItem> stockItems = stockService.getItemsExpiringSoon();
        return ResponseEntity.ok(stockItems);
    }
        
    @GetMapping("/report/stock-summary")
    @Operation(summary = "stock-summary", description = "stock-summary with statistces")
    public ResponseEntity<Map<String, Object>> getStockSummary() {
        Map<String, Object> summary = stockService.getStockSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/report/stock-value")
    @Operation(summary = "stock-value", description = "")
    public ResponseEntity<Map<String, Object>> getStockValue() {
        Map<String, Object> stockValue = stockService.getStockValue();
        return ResponseEntity.ok(stockValue);
    }
    
    
    @GetMapping("/report/{productType}")
    @Operation(summary = "report by product type", description = "get report by product type")
    public ResponseEntity<Map<String, Object>> getStockReportByProductType(@PathVariable ProductType productType) {
        Map<String, Object> report = stockService.getStockReportByProductType(productType);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/report/comprehensive")
    @Operation(summary = "comprehensive report for stock", description = "get comprehensive report for stock")
    public ResponseEntity<Map<String, Object>> getComprehensiveStockReport() {
        Map<String, Object> report = stockService.getComprehensiveStockReport();
        return ResponseEntity.ok(report);
    }
    
    
    @GetMapping("/all-with-product-info")
    @Operation(summary = "all stock with product information", description = "get all stock with product information")
    public ResponseEntity<List<StockItemDTOResponse>> getAllStockItemsWithProductInfo() {
        List<StockItemDTOResponse> stockItems = stockService.getAllStockItemsWithProductInfo();
        return ResponseEntity.ok(stockItems);   
    }
    
    @GetMapping("/product/{productId}/details")
    @Operation(summary = "product with its stock details", description = "get product with its stock details")
    public ResponseEntity<Map<String, Object>> getProductStockDetails(@PathVariable Long productId) {
        Map<String, Object> productDetails = stockService.getProductStockDetails(productId);
        return ResponseEntity.ok(productDetails);
    }
    
    @GetMapping("/{stockItemId}/detail")
    @Operation(summary = "Get stock item detail", description = "Get detailed information about a specific stock item")
    public ResponseEntity<StockItemDetailDTOResponse> getStockItemDetail(@PathVariable Long stockItemId) {
        StockItemDetailDTOResponse stockItemDetail = stockService.getStockItemDetail(stockItemId);
        return ResponseEntity.ok(stockItemDetail);
    }
    
} 