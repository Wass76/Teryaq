package com.Teryaq.product.mapper;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockReportDTOResponse;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.service.StockManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockItemMapper {
    
    private final StockManagementService stockManagementService;
    
    public StockItemDTOResponse toResponse(StockItem stockItem) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = stockItem.getExpiryDate();
        
        Boolean isExpired = expiryDate != null && expiryDate.isBefore(today);
        Boolean isExpiringSoon = expiryDate != null && 
            expiryDate.isAfter(today) && 
            expiryDate.isBefore(today.plusDays(30));
        
        Integer daysUntilExpiry = expiryDate != null ? 
            (int) ChronoUnit.DAYS.between(today, expiryDate) : null;
        
        String productName = stockManagementService.getProductName(
            stockItem.getProductId(), stockItem.getProductType());
        
        return StockItemDTOResponse.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .productName(productName)
                .productType(stockItem.getProductType())
                .quantity(stockItem.getQuantity())
                .bonusQty(stockItem.getBonusQty())
                .expiryDate(stockItem.getExpiryDate())
                .batchNo(stockItem.getBatchNo())
                .actualPurchasePrice(stockItem.getActualPurchasePrice())
                .dateAdded(stockItem.getDateAdded())
                .addedBy(stockItem.getCreatedBy() != null ? stockItem.getCreatedBy() : stockItem.getAddedBy())
                .purchaseInvoiceId(stockItem.getPurchaseInvoice() != null ? 
                    stockItem.getPurchaseInvoice().getId() : null)
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .daysUntilExpiry(daysUntilExpiry)
                .build();
    }
    
    public List<StockItemDTOResponse> toResponseList(List<StockItem> stockItems) {
        return stockItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public StockReportDTOResponse toReportResponse(List<StockItem> stockItems, 
                                                   ProductType productType) {
        List<StockItemDTOResponse> stockItemResponses = toResponseList(stockItems);
        
        Integer totalQuantity = stockItems.stream()
                .mapToInt(StockItem::getQuantity)
                .sum();
        
        Double totalValue = stockItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice())
                .sum();
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        Long expiredItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(today))
                .count();
        
        Long expiringSoonItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && 
                    item.getExpiryDate().isAfter(today) && 
                    item.getExpiryDate().isBefore(thirtyDaysFromNow))
                .count();
        
        return StockReportDTOResponse.builder()
                .productType(productType)
                .totalItems(stockItems.size())
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .expiredItems(expiredItems)
                .expiringSoonItems(expiringSoonItems)
                .stockItems(stockItemResponses)
                .build();
    }
} 