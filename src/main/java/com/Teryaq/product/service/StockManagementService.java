package com.Teryaq.product.service;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class StockManagementService extends BaseSecurityService {

    private final StockItemRepo stockItemRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;


    public StockManagementService(StockItemRepo stockItemRepo,
                                PharmacyProductRepo pharmacyProductRepo,
                                MasterProductRepo masterProductRepo,
                                UserRepository userRepository) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.masterProductRepo = masterProductRepo;
    }


    public Integer getTotalQuantityByProductId(Long productId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.getTotalQuantityByProductIdAndPharmacyId(productId, currentPharmacyId);
    }
    
   
    public List<StockItem> getStockItemsByProductId(Long productId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.findByProductIdAndPharmacyId(productId, currentPharmacyId);
    }
    
    
    public List<StockItem> getAvailableStockByProductId(Long productId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDate today = LocalDate.now();
        return stockItemRepo.findByProductIdAndPharmacyIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(
            productId, currentPharmacyId, 0, today);
    }
    
    
    public List<StockItem> getExpiredItems() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.findExpiredItemsByPharmacyId(LocalDate.now(), currentPharmacyId);
    }
   
    public List<StockItem> getItemsExpiringSoon() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return stockItemRepo.findItemsExpiringSoonByPharmacyId(today, thirtyDaysFromNow, currentPharmacyId);
    }
    
   
    public Map<String, Object> getStockReportByProductType(ProductType productType) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByProductTypeAndPharmacyId(productType, currentPharmacyId);
        
        Map<String, Object> report = new HashMap<>();
        report.put("productType", productType);
        report.put("totalItems", stockItems.size());
        report.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        report.put("totalValue", stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum());
        
        long expiredCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
            .count();
        report.put("expiredItems", expiredCount);
        
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        long expiringSoonCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && 
                item.getExpiryDate().isAfter(LocalDate.now()) && 
                item.getExpiryDate().isBefore(thirtyDaysFromNow))
            .count();
        report.put("expiringSoonItems", expiringSoonCount);
        
        return report;
    }
    
 
    public boolean isQuantityAvailable(Long productId, Integer requiredQuantity) {
        Integer availableQuantity = getTotalQuantityByProductId(productId);
        return availableQuantity >= requiredQuantity;
    }
    
  
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
 
    public Map<String, Object> getComprehensiveStockReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("pharmacyProducts", getStockReportByProductType(ProductType.PHARMACY));
        
        report.put("masterProducts", getStockReportByProductType(ProductType.MASTER));
        
        report.put("expiredItems", getExpiredItems());
        
        report.put("expiringSoonItems", getItemsExpiringSoon());
        
        return report;
    }
    
  
    public List<StockItem> getStockItemsForSale(Long productId, Integer requiredQuantity) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDate today = LocalDate.now();
        List<StockItem> availableStock = stockItemRepo
            .findByProductIdAndPharmacyIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(productId, currentPharmacyId, 0, today);
        
        // تطبيق FIFO - ترتيب حسب تاريخ الإضافة (الأقدم أولاً)
        return availableStock.stream()
            .filter(item -> item.getQuantity() > 0)
            .collect(Collectors.toList());
    }

  
    public List<StockItem> getAllStockItems() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.findByPharmacyId(currentPharmacyId);
    }

   
    public List<Map<String, Object>> getAllStockItemsWithProductInfo() {
        List<StockItem> stockItems = getAllStockItems();
        
        return stockItems.stream().map(item -> {
            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("id", item.getId());
            stockInfo.put("productId", item.getProductId());
            stockInfo.put("productType", item.getProductType());
            stockInfo.put("quantity", item.getQuantity());
            stockInfo.put("actualPurchasePrice", item.getActualPurchasePrice());
            
            // تنسيق التواريخ كـ strings
            if (item.getExpiryDate() != null) {
                stockInfo.put("expiryDate", item.getExpiryDate().toString());
            } else {
                stockInfo.put("expiryDate", null);
            }
            
            if (item.getDateAdded() != null) {
                stockInfo.put("dateAdded", item.getDateAdded().toString());
            } else {
                stockInfo.put("dateAdded", null);
            }
            
            // إضافة اسم المنتج
            String productName = getProductName(item.getProductId(), item.getProductType());
            stockInfo.put("productName", productName);
            
            return stockInfo;
        }).collect(Collectors.toList());
    }
} 