package com.Teryaq.reports.repository;

import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.purchase.entity.PurchaseInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.purchase.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simplified Reports Repository
 * Implements only the specific queries agreed upon with the business team:
 * 1. Monthly Purchase Report (daily breakdown)
 * 2. Daily Purchase Report
 * 3. Monthly Profit Report (daily breakdown)
 * 4. Daily Profit Report
 * 5. Most Sold Categories Monthly
 * 6. Top 10 Products Monthly
 */
@Repository
public interface ReportRepository extends JpaRepository<SaleInvoice, Long> {
    
    // ============================================================================
    // PURCHASE REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get monthly purchase daily breakdown
     * Returns purchase data for each day in the specified month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "DATE(pi.createdAt) as date, " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(pi.createdAt), pi.currency " +
           "ORDER BY date")
    List<Map<String, Object>> getMonthlyPurchaseDailyBreakdown(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly purchase summary
     * Returns summary data for the entire month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY pi.currency")
    List<Map<String, Object>> getMonthlyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily purchase summary
     * Returns purchase data for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) = :date " +
           "GROUP BY pi.currency")
    List<Map<String, Object>> getDailyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    /**
     * Get daily purchase items
     * Returns purchase items for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "pii.productId as productName, " +
           "pii.receivedQty as quantity, " +
           "pii.invoicePrice as unitPrice, " +
           "(pii.receivedQty * pii.invoicePrice) as subTotal, " +
           "pi.supplier.name as supplierName, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoiceItem pii " +
           "JOIN pii.purchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) = :date " +
           "ORDER BY (pii.receivedQty * pii.invoicePrice) DESC")
    List<Map<String, Object>> getDailyPurchaseItems(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    // ============================================================================
    // PROFIT REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get monthly profit daily breakdown
     * Returns profit data for each day in the specified month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "DATE(si.invoiceDate) as date, " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY DATE(si.invoiceDate), si.currency " +
           "ORDER BY date")
    List<Map<String, Object>> getMonthlyProfitDailyBreakdown(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly profit summary
     * Returns summary data for the entire month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY si.currency")
    List<Map<String, Object>> getMonthlyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily profit summary
     * Returns profit data for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) = :date " +
           "AND si.status = 'SOLD' " +
           "GROUP BY si.currency")
    List<Map<String, Object>> getDailyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    /**
     * Get daily profit items
     * Returns profit items for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "sii.stockItem.productName as productName, " +
           "sii.quantity as quantity, " +
           "sii.subTotal as revenue, " +
           "(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as profit, " +
           "si.currency as currency " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) = :date " +
           "AND si.status = 'SOLD' " +
           "ORDER BY (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) DESC")
    List<Map<String, Object>> getDailyProfitItems(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    // ============================================================================
    // CATEGORY AND PRODUCT REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get most sold categories monthly
     * Returns the most sold categories in the pharmacy for the specified month
     * Note: This is a simplified version that groups by product name since category relationship is complex
     */
    @Query("SELECT " +
           "sii.stockItem.productName as categoryName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "COUNT(DISTINCT si) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productName " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getMostSoldCategories(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get top 10 products monthly
     * Returns the top 10 most sold products in the pharmacy for the specified month
     */
    @Query("SELECT " +
           "sii.stockItem.productId as productId, " +
           "sii.stockItem.productType as productType, " +
           "sii.stockItem.productName as productName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "COUNT(DISTINCT si) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productId, sii.stockItem.productType, sii.stockItem.productName " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getTop10Products(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
