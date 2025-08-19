package com.Teryaq.product.service;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockItemDetailDTOResponse;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.utils.exception.UnAuthorizedException;
import com.Teryaq.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.Teryaq.product.mapper.StockItemMapper;
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class StockService extends BaseSecurityService {

    private final StockItemRepo stockItemRepo;
    private final StockItemMapper stockItemMapper;

    public StockService(StockItemRepo stockItemRepo,
                                @Lazy StockItemMapper stockItemMapper,
                                UserRepository userRepository) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
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
    
    public StockItemDTOResponse editStockQuantityAndExpiryDate(Long stockItemId, Integer newQuantity, 
                                                              LocalDate newExpiryDate, Integer newMinStockLevel, 
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
        
        if (newQuantity != null && newQuantity < 0) {
            throw new ConflictException("the quantity can't be negative");
        }
        
        if (newExpiryDate != null && newExpiryDate.isBefore(LocalDate.now())) {
            throw new ConflictException("expiry date cannot be in the past");
        }
        
        if (newMinStockLevel != null && newMinStockLevel < 0) {
            throw new ConflictException("minimum stock level cannot be negative");
        }
        
        if (newQuantity != null) {
            stockItem.setQuantity(newQuantity);
            
            if (newQuantity == 0) {
                stockItemRepo.delete(stockItem);
                return null;
            }
        }
        
        if (newExpiryDate != null) {
            stockItem.setExpiryDate(newExpiryDate);
        }
        
        if (newMinStockLevel != null) {
            stockItem.setMinStockLevel(newMinStockLevel);
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
        List<StockItem> stockItems = stockItemRepo.searchStockItems(keyword, currentPharmacyId);
        return stockItemMapper.toResponseList(stockItems);
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
    
  

    public List<StockItemDTOResponse> getAllStockItems() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        return stockItemMapper.toResponseList(stockItems);
    }

    public List<StockItemDTOResponse> getAllStockItemsWithProductInfo() {
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(getCurrentUserPharmacyId());
        return stockItemMapper.toResponseList(stockItems);
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
        details.put("stockItems", stockItemMapper.toResponseList(stockItems));
        
        return details;
    }
    
    public StockItemDetailDTOResponse getStockItemDetail(Long stockItemId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("Stock item not found"));
        
        if (!stockItem.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("You can't access stock item from another pharmacy");
        }
        
        return stockItemMapper.toDetailResponse(stockItem);
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
            .mapToDouble(item -> item.getQuantity() * stockItemMapper.getProductSellingPrice(item.getProductId(), item.getProductType()))
            .sum();
        
        stockValue.put("totalPurchaseValue", totalPurchaseValue);
        stockValue.put("totalSellingValue", totalSellingValue);
        stockValue.put("potentialProfit", totalSellingValue - totalPurchaseValue);
        stockValue.put("profitMargin", totalPurchaseValue > 0 ? ((totalSellingValue - totalPurchaseValue) / totalPurchaseValue) * 100 : 0);
        
        return stockValue;
    }

    public StockItemDTOResponse deleteStockItem(Long stockItemId) {
        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("stock item not found"));
        
        stockItemRepo.delete(stockItem);
        return stockItemMapper.toResponse(stockItem);
    }

} 