package com.Teryaq.product.repo;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockItemRepo extends JpaRepository<StockItem, Long> {
    
    // البحث عن مخزون منتج معين مع كمية متوفرة
    List<StockItem> findByProductIdAndQuantityGreaterThanOrderByDateAddedAsc(Long productId, Integer minQuantity);
    
    // البحث عن مخزون منتج معين مع تاريخ انتهاء صالح
    List<StockItem> findByProductIdAndExpiryDateAfterOrderByDateAddedAsc(Long productId, LocalDate date);
    
    // البحث عن مخزون منتج معين مع كمية متوفرة وتاريخ انتهاء صالح
    List<StockItem> findByProductIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(
        Long productId, Integer minQuantity, LocalDate date);
    
    // البحث عن مخزون حسب نوع المنتج
    List<StockItem> findByProductType(ProductType productType);
    
    // البحث عن مخزون منتج معين
    List<StockItem> findByProductId(Long productId);
    
    // حساب إجمالي الكمية المتوفرة لمنتج معين
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s WHERE s.productId = :productId AND s.quantity > 0")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);
    
    // البحث عن منتجات منتهية الصلاحية
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate < :date AND s.quantity > 0")
    List<StockItem> findExpiredItems(@Param("date") LocalDate date);
    
    // البحث عن منتجات قريبة من انتهاء الصلاحية (خلال 30 يوم)
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate BETWEEN :startDate AND :endDate AND s.quantity > 0")
    List<StockItem> findItemsExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Pharmacy-specific queries
    List<StockItem> findByPharmacyId(Long pharmacyId);
    
    List<StockItem> findByProductIdAndPharmacyId(Long productId, Long pharmacyId);
    
    List<StockItem> findByProductTypeAndPharmacyId(ProductType productType, Long pharmacyId);
    
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    Integer getTotalQuantityByProductIdAndPharmacyId(@Param("productId") Long productId, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate < :date AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findExpiredItemsByPharmacyId(@Param("date") LocalDate date, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate BETWEEN :startDate AND :endDate AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findItemsExpiringSoonByPharmacyId(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > :minQuantity AND s.expiryDate > :date ORDER BY s.dateAdded ASC")
    List<StockItem> findByProductIdAndPharmacyIdAndQuantityGreaterThanAndExpiryDateAfterOrderByDateAddedAsc(
        @Param("productId") Long productId, 
        @Param("pharmacyId") Long pharmacyId, 
        @Param("minQuantity") Integer minQuantity, 
        @Param("date") LocalDate date);
} 