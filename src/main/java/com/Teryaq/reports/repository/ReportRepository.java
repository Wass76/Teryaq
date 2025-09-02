package com.Teryaq.reports.repository;

import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.user.entity.CustomerDebt;
import com.Teryaq.purchase.entity.PurchaseInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.purchase.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repository for Reports Database Queries
 * Implements all database queries for the 5 report categories
 */
@Repository
public interface ReportRepository extends JpaRepository<SaleInvoice, Long> {
    
    // 3.5.1 Sales Reports Queries
    
    /**
     * Daily Sales Summary - تقارير المبيعات اليومية
     * إجمالي المبيعات، عدد الفواتير، المنتجات الأكثر مبيعاً
     */
    @Query("SELECT " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalSales, " +
           "SUM(si.paidAmount) as totalPaid " +
           "FROM SaleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD'")
    Map<String, Object> getDailySalesSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Monthly Sales Summary - تقارير المبيعات الشهرية
     * إجمالي المبيعات، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @Query("SELECT " +
           "YEAR(si.invoiceDate) as year, " +
           "MONTH(si.invoiceDate) as month, " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalSales " +
           "FROM SaleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND si.invoiceDate BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY YEAR(si.invoiceDate), MONTH(si.invoiceDate) " +
           "ORDER BY year DESC, month DESC")
    List<Map<String, Object>> getMonthlySalesSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Best Selling Products - المنتجات الأكثر مبيعاً
     */
    @Query("SELECT " +
           "sii.stockItem.productName as productName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productName " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getBestSellingProducts(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 3.5.2 Profit Reports Queries
    
    /**
     * Daily Profit Summary - تقارير الأرباح اليومية
     * إجمالي الأرباح، نسبة الربح، المنتجات الأكثر ربحية
     */
    @Query("SELECT " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "COUNT(si) as totalInvoices " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD'")
    Map<String, Object> getDailyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Monthly Profit Summary - تقارير الأرباح الشهرية
     * إجمالي الأرباح، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @Query("SELECT " +
           "YEAR(si.invoiceDate) as year, " +
           "MONTH(si.invoiceDate) as month, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND si.invoiceDate BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY YEAR(si.invoiceDate), MONTH(si.invoiceDate) " +
           "ORDER BY year DESC, month DESC")
    List<Map<String, Object>> getMonthlyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Most Profitable Products - المنتجات الأكثر ربحية
     */
    @Query("SELECT " +
           "sii.stockItem.productName as productName, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "SUM(sii.quantity) as totalQuantity " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productName " +
           "ORDER BY totalProfit DESC")
    List<Map<String, Object>> getMostProfitableProducts(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 3.5.3 Inventory Reports Queries
    
    /**
     * Current Inventory Summary - تقارير المخزون الحالي
     * الكميات المتوفرة، المنتجات منخفضة المخزون، المنتجات قريبة من انتهاء الصلاحية
     */
    @Query("SELECT " +
           "COUNT(si) as totalProducts, " +
           "SUM(si.quantity) as totalQuantity, " +
           "SUM(si.quantity * si.actualPurchasePrice) as totalValue " +
           "FROM StockItem si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND si.quantity > 0")
    Map<String, Object> getCurrentInventorySummary(@Param("pharmacyId") Long pharmacyId);
    
    /**
     * Low Stock Products - المنتجات منخفضة المخزون
     */
    @Query("SELECT " +
           "si.productName as productName, " +
           "si.quantity as currentQuantity, " +
           "si.minStockLevel as minStockLevel, " +
           "si.batchNo as batchNo, " +
           "si.expiryDate as expiryDate " +
           "FROM StockItem si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND si.quantity <= si.minStockLevel " +
           "AND si.quantity > 0 " +
           "ORDER BY si.quantity ASC")
    List<Map<String, Object>> getLowStockProducts(@Param("pharmacyId") Long pharmacyId);
    
    /**
     * Expiring Products - المنتجات قريبة من انتهاء الصلاحية
     */
    @Query("SELECT " +
           "si.productName as productName, " +
           "si.quantity as currentQuantity, " +
           "si.expiryDate as expiryDate, " +
           "si.batchNo as batchNo " +
           "FROM StockItem si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND si.expiryDate BETWEEN :startDate AND :endDate " +
           "AND si.quantity > 0 " +
           "ORDER BY si.expiryDate ASC")
    List<Map<String, Object>> getExpiringProducts(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Inventory Movement - تقارير حركة المخزون
     * المنتجات الأكثر دوراناً، المنتجات الراكدة
     */
    @Query("SELECT " +
           "sii.stockItem.productName as productName, " +
           "SUM(sii.quantity) as totalSold, " +
           "COUNT(DISTINCT si.id) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productName " +
           "ORDER BY totalSold DESC")
    List<Map<String, Object>> getInventoryMovement(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 3.5.4 Debt Reports Queries
    
    /**
     * Customer Debt Summary - تقارير ديون العملاء
     * إجمالي الديون، العملاء الأكثر مديونية، الديون المتأخرة
     */
    @Query("SELECT " +
           "COUNT(cd) as totalDebts, " +
           "SUM(cd.remainingAmount) as totalDebtAmount, " +
           "SUM(CASE WHEN cd.status = 'OVERDUE' THEN cd.remainingAmount ELSE 0 END) as overdueAmount " +
           "FROM CustomerDebt cd " +
           "JOIN cd.customer c " +
           "WHERE c.pharmacy.id = :pharmacyId " +
           "AND cd.status IN ('ACTIVE', 'OVERDUE')")
    Map<String, Object> getCustomerDebtSummary(@Param("pharmacyId") Long pharmacyId);
    
    /**
     * Most Indebted Customers - العملاء الأكثر مديونية
     */
    @Query("SELECT " +
           "c.name as customerName, " +
           "c.phoneNumber as customerPhone, " +
           "SUM(cd.remainingAmount) as totalDebt, " +
           "COUNT(cd) as debtCount " +
           "FROM CustomerDebt cd " +
           "JOIN cd.customer c " +
           "WHERE c.pharmacy.id = :pharmacyId " +
           "AND cd.status IN ('ACTIVE', 'OVERDUE') " +
           "GROUP BY c.id, c.name, c.phoneNumber " +
           "ORDER BY totalDebt DESC")
    List<Map<String, Object>> getMostIndebtedCustomers(@Param("pharmacyId") Long pharmacyId);
    
    /**
     * Overdue Debts - الديون المتأخرة
     */
    @Query("SELECT " +
           "c.name as customerName, " +
           "c.phoneNumber as customerPhone, " +
           "cd.remainingAmount as debtAmount, " +
           "cd.dueDate as dueDate, " +
           "cd.notes as notes " +
           "FROM CustomerDebt cd " +
           "JOIN cd.customer c " +
           "WHERE c.pharmacy.id = :pharmacyId " +
           "AND cd.status = 'OVERDUE' " +
           "ORDER BY cd.dueDate ASC")
    List<Map<String, Object>> getOverdueDebts(@Param("pharmacyId") Long pharmacyId);
    
    // 3.5.5 Purchase Reports Queries
    
    /**
     * Daily Purchase Summary - تقارير الشراء اليومية
     * إجمالي قيمة عمليات الشراء
     */
    @Query("SELECT " +
           "COUNT(pi) as totalPurchases, " +
           "SUM(pi.total) as totalAmount " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate")
    Map<String, Object> getDailyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Monthly Purchase Summary - تقارير الشراء الشهرية
     * إجمالي قيمة عمليات الشراء، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @Query("SELECT " +
           "YEAR(pi.createdAt) as year, " +
           "MONTH(pi.createdAt) as month, " +
           "COUNT(pi) as totalPurchases, " +
           "SUM(pi.total) as totalAmount " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND pi.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(pi.createdAt), MONTH(pi.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Map<String, Object>> getMonthlyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Purchase by Supplier - المشتريات حسب المورد
     */
    @Query("SELECT " +
           "s.name as supplierName, " +
           "COUNT(pi) as purchaseCount, " +
           "SUM(pi.total) as totalAmount " +
           "FROM PurchaseInvoice pi " +
           "JOIN pi.supplier s " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY s.id, s.name " +
           "ORDER BY totalAmount DESC")
    List<Map<String, Object>> getPurchaseBySupplier(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
