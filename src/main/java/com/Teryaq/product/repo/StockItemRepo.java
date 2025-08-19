package com.Teryaq.product.repo;

import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.Enum.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockItemRepo extends JpaRepository<StockItem, Long> {
    
    List<StockItem> findByProductId(Long productId);
    
    List<StockItem> findByPharmacyId(Long pharmacyId);
    
    List<StockItem> findByProductIdAndPharmacyId(Long productId, Long pharmacyId);
    
    List<StockItem> findByProductTypeAndPharmacyId(ProductType productType, Long pharmacyId);
    
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > 0 AND s.productType = :productType")
    Integer getTotalQuantity(@Param("productId") Long productId, @Param("pharmacyId") Long pharmacyId, @Param("productType") ProductType productType);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate < :date AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findExpiredItems(@Param("date") LocalDate date, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate BETWEEN :startDate AND :endDate AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findItemsExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > :minQuantity AND s.expiryDate > :date ORDER BY s.dateAdded ASC")
    List<StockItem> findByProductIdAndQuantity(
        @Param("productId") Long productId, 
        @Param("pharmacyId") Long pharmacyId, 
        @Param("minQuantity") Integer minQuantity, 
        @Param("date") LocalDate date);
    
    @Query("""
        SELECT si FROM StockItem si
        LEFT JOIN si.purchaseInvoice pi
        WHERE si.pharmacy.id = :pharmacyId
          AND (
            LOWER(si.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(si.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR EXISTS (
                SELECT 1 FROM PharmacyProduct pp 
                WHERE pp.id = si.productId 
                AND si.productType = com.Teryaq.product.Enum.ProductType.PHARMACY
                AND (
                    LOWER(pp.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR EXISTS (
                        SELECT 1 FROM PharmacyProductBarcode ppb 
                        WHERE ppb.product.id = pp.id 
                        AND LOWER(ppb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                )
            )
            OR EXISTS (
                SELECT 1 FROM MasterProduct mp 
                WHERE mp.id = si.productId 
                AND si.productType = com.Teryaq.product.Enum.ProductType.MASTER
                AND (
                    LOWER(mp.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(mp.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            )
          )
        """)
        List<StockItem> searchStockItems(
            @Param("keyword") String keyword,
            @Param("pharmacyId") Long pharmacyId);
    
    List<StockItem> findByBarcodeAndPharmacyId(String barcode, Long pharmacyId);
    
    List<StockItem> findByProductNameContainingIgnoreCaseAndPharmacyId(String productName, Long pharmacyId);
    
    // @Query("""
    //     SELECT new com.Teryaq.product.dto.StockItemDTOResponse(
    //         si.id, si.productId, null, si.productType, 
    //         si.quantity, si.bonusQty, si.expiryDate, si.batchNo, 
    //         si.actualPurchasePrice, si.dateAdded, si.addedBy, 
    //         pi.id, null, null, null, si.pharmacy.id, pi.invoiceNumber)
    //     FROM StockItem si
    //     LEFT JOIN si.purchaseInvoice pi
    //     WHERE si.pharmacy.id = :pharmacyId
    //       AND LOWER(pi.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
    //     """)
    //     List<StockItemDTOResponse> searchStockItems(
    //         @Param("keyword") String keyword,
    //         @Param("pharmacyId") Long pharmacyId);
    
   
}
            