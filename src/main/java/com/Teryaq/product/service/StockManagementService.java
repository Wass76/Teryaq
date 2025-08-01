package com.Teryaq.product.service;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockManagementService {
    
    private final StockItemRepo stockItemRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    
    /**
     * الحصول على إجمالي الكمية المتوفرة لمنتج معين
     */
    public Integer getTotalQuantityByProductId(Long productId) {
        return stockItemRepo.getTotalQuantityByProductId(productId);
    }
    
    /**
     * الحصول على تفاصيل المخزون لمنتج معين
     */
    public List<StockItem> getStockItemsByProductId(Long productId) {
        return stockItemRepo.findByProductId(productId);
    }
    
    /**
     * الحصول على المخزون المتوفر لمنتج معين (كمية > 0 وتاريخ انتهاء صالح)
     */
    public List<StockItem> getAvailableStockByProductId(Long productId) {
        LocalDate today = LocalDate.now();
        return stockItemRepo.findByProductIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(
            productId, 0, today);
    }
    
    /**
     * الحصول على المنتجات منتهية الصلاحية
     */
    public List<StockItem> getExpiredItems() {
        return stockItemRepo.findExpiredItems(LocalDate.now());
    }
    
    /**
     * الحصول على المنتجات القريبة من انتهاء الصلاحية (خلال 30 يوم)
     */
    public List<StockItem> getItemsExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return stockItemRepo.findItemsExpiringSoon(today, thirtyDaysFromNow);
    }
    
    /**
     * الحصول على تقرير المخزون حسب نوع المنتج
     */
    public Map<String, Object> getStockReportByProductType(ProductType productType) {
        List<StockItem> stockItems = stockItemRepo.findByProductType(productType);
        
        Map<String, Object> report = new HashMap<>();
        report.put("productType", productType);
        report.put("totalItems", stockItems.size());
        report.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        report.put("totalValue", stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum());
        
        // حساب المنتجات منتهية الصلاحية
        long expiredCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
            .count();
        report.put("expiredItems", expiredCount);
        
        // حساب المنتجات قريبة من انتهاء الصلاحية
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        long expiringSoonCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && 
                item.getExpiryDate().isAfter(LocalDate.now()) && 
                item.getExpiryDate().isBefore(thirtyDaysFromNow))
            .count();
        report.put("expiringSoonItems", expiringSoonCount);
        
        return report;
    }
    
    /**
     * التحقق من توفر الكمية المطلوبة لمنتج معين
     */
    public boolean isQuantityAvailable(Long productId, Integer requiredQuantity) {
        Integer availableQuantity = stockItemRepo.getTotalQuantityByProductId(productId);
        return availableQuantity >= requiredQuantity;
    }
    
    /**
     * الحصول على اسم المنتج حسب النوع
     */
    public String getProductName(Long productId, ProductType productType) {
        if (productType == ProductType.PHARMACY) {
            return pharmacyProductRepo.findById(productId)
                .map(product -> product.getTradeName())
                .orElse("Unknown Product");
        } else if (productType == ProductType.MASTER) {
            return masterProductRepo.findById(productId)
                .map(product -> product.getTradeName())
                .orElse("Unknown Product");
        }
        return "Unknown Product";
    }
    
    /**
     * الحصول على تقرير شامل للمخزون
     */
    public Map<String, Object> getComprehensiveStockReport() {
        Map<String, Object> report = new HashMap<>();
        
        // تقرير المنتجات الصيدلية
        report.put("pharmacyProducts", getStockReportByProductType(ProductType.PHARMACY));
        
        // تقرير المنتجات الرئيسية
        report.put("masterProducts", getStockReportByProductType(ProductType.MASTER));
        
        // المنتجات منتهية الصلاحية
        report.put("expiredItems", getExpiredItems());
        
        // المنتجات قريبة من انتهاء الصلاحية
        report.put("expiringSoonItems", getItemsExpiringSoon());
        
        return report;
    }
    
    /**
     * البحث عن مخزون منتج معين مع تطبيق FIFO
     */
    public List<StockItem> getStockItemsForSale(Long productId, Integer requiredQuantity) {
        LocalDate today = LocalDate.now();
        List<StockItem> availableStock = stockItemRepo
            .findByProductIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(productId, 0, today);
        
        // تطبيق FIFO - ترتيب حسب تاريخ الإضافة (الأقدم أولاً)
        return availableStock.stream()
            .filter(item -> item.getQuantity() > 0)
            .collect(Collectors.toList());
    }
} 