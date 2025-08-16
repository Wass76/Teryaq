package com.Teryaq.product.service;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.utils.exception.UnAuthorizedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.Teryaq.product.mapper.StockItemMapper;
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class StockService extends BaseSecurityService {

    private final StockItemRepo stockItemRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final StockItemMapper stockItemMapper;

    public StockService(StockItemRepo stockItemRepo,
                                PharmacyProductRepo pharmacyProductRepo,
                                MasterProductRepo masterProductRepo,
                                @Lazy StockItemMapper stockItemMapper,
                                UserRepository userRepository) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.masterProductRepo = masterProductRepo;
        this.stockItemMapper = stockItemMapper;
    }

    public StockItemDTOResponse editStockQuantity(Long stockItemId, Integer newQuantity, 
                                       String reasonCode, String additionalNotes) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("only pharmacy employees can edit the stock");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("the employee is not associated with any pharmacy");
        }
        

        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("stock item not found"));
        
        if (!stockItem.getPharmacy().getId().equals(employee.getPharmacy().getId())) {
            throw new UnAuthorizedException("you can't edit stock of another pharmacy");
        }
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("the quantity can't be negative");
        }
        
        stockItem.setQuantity(newQuantity);
        
        if (newQuantity == 0) {
            stockItemRepo.delete(stockItem);
            return null;
        }
        
        stockItem.setLastModifiedBy(currentUser.getId());
        stockItem.setUpdatedAt(LocalDateTime.now());
        
        StockItem savedStockItem = stockItemRepo.save(stockItem);
        
        StockItemDTOResponse response = stockItemMapper.toResponse(savedStockItem);
        
        response.setPharmacyId(savedStockItem.getPharmacy().getId());
        
        if (savedStockItem.getPurchaseInvoice() != null) {
            response.setPurchaseInvoiceNumber(savedStockItem.getPurchaseInvoice().getInvoiceNumber());
        }
        
        return response;
    }
    
    public List<StockItemDTOResponse> stockItemSearch(String keyword) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItemDTOResponse> stockItems = stockItemRepo.searchStockItems(keyword, currentPharmacyId);
        
        stockItems.forEach(item -> {
            if (item.getProductId() != null && item.getProductType() != null) {
                String productName = getProductName(item.getProductId(), item.getProductType());
                item.setProductName(productName);
            }
        });
        
        return stockItems;
    }
    
    public List<StockItem> getExpiredItems() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.findExpiredItems(LocalDate.now(), currentPharmacyId);
    }
   
    public List<StockItem> getItemsExpiringSoon() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return stockItemRepo.findItemsExpiringSoon(today, thirtyDaysFromNow, currentPharmacyId);
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
    

 
    public Map<String, Object> getComprehensiveStockReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("pharmacyProducts", getStockReportByProductType(ProductType.PHARMACY));
        
        report.put("masterProducts", getStockReportByProductType(ProductType.MASTER));
        
        report.put("expiredItems", getExpiredItems());
        
        report.put("expiringSoonItems", getItemsExpiringSoon());
        
        return report;
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
            stockInfo.put("batchNo",item.getBatchNo());
            
            
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
            
            String productName = getProductName(item.getProductId(), item.getProductType());
            stockInfo.put("productName", productName);
            
            boolean requiresPrescription = isProductRequiresPrescription(item.getProductId(), item.getProductType());
            stockInfo.put("requiresPrescription", requiresPrescription);

            Float sellingPrice = getProductSellingPrice(item.getProductId(), item.getProductType());
            stockInfo.put("sellingPrice", sellingPrice);
            
            return stockInfo;
        }).collect(Collectors.toList());
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

    public boolean isProductRequiresPrescription(Long productId, ProductType productType) {
        if (productType == ProductType.PHARMACY) {
            return pharmacyProductRepo.findById(productId)
                .map(product -> product.getRequiresPrescription()) 
                .orElse(false);
        } else if (productType == ProductType.MASTER) {
            return masterProductRepo.findById(productId)
                .map(product -> product.getRequiresPrescription()) 
                .orElse(false);
        }
        return false;
    }
    
    public Float getProductSellingPrice(Long productId, ProductType productType) {
        if (productType == ProductType.PHARMACY) {
            return pharmacyProductRepo.findById(productId)
                .map(product -> product.getRefSellingPrice())
                .orElse(0f);
        } else if (productType == ProductType.MASTER) {
            return masterProductRepo.findById(productId)
                .map(product -> product.getRefSellingPrice())
                .orElse(0f);
        }
        return 0f;
    }

    public boolean isQuantityAvailable(Long productId, Integer requiredQuantity, ProductType productType) {
        Integer availableQuantity = stockItemRepo.getTotalQuantity(productId, getCurrentUserPharmacyId(), productType);
        return availableQuantity >= requiredQuantity;
    }
    
    
    public Map<String, Object> getProductStockDetails(Long productId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByProductIdAndPharmacyId(productId, currentPharmacyId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("productId", productId);
        details.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        details.put("stockItems", stockItems);
        
        if (!stockItems.isEmpty()) {
            ProductType productType = stockItems.get(0).getProductType();
            details.put("productName", getProductName(productId, productType));
            details.put("sellingPrice", getProductSellingPrice(productId, productType));
            details.put("productType", productType.toString());
        }
        
        return details;
    }
        
    public Map<String, Object> getStockSummary() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", stockItems.size());
        summary.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        summary.put("expiredProducts", getExpiredItems().size());
        summary.put("expiringSoonProducts", getItemsExpiringSoon().size());
        summary.put("totalValue", stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum());
        
        return summary;
    }
    
    public Map<String, Object> getStockValue() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        
        Map<String, Object> stockValue = new HashMap<>();
        double totalPurchaseValue = stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum();
        double totalSellingValue = stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * getProductSellingPrice(item.getProductId(), item.getProductType()))
            .sum();
        
        stockValue.put("totalPurchaseValue", totalPurchaseValue);
        stockValue.put("totalSellingValue", totalSellingValue);
        stockValue.put("potentialProfit", totalSellingValue - totalPurchaseValue);
        stockValue.put("profitMargin", totalPurchaseValue > 0 ? ((totalSellingValue - totalPurchaseValue) / totalPurchaseValue) * 100 : 0);
        
        return stockValue;
    }

} 