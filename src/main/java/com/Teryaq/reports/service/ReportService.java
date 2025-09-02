package com.Teryaq.reports.service;

import com.Teryaq.reports.dto.request.ReportRequest;
import com.Teryaq.reports.dto.response.ReportResponse;
import com.Teryaq.reports.enums.ReportType;
import com.Teryaq.reports.repository.ReportRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Service for handling report requirements
 * Implements all 5 report categories:
 * 3.5.1 Sales Reports (تقارير المبيعات)
 * 3.5.2 Profit Reports (تقارير الأرباح)
 * 3.5.3 Inventory Reports (تقارير المخزون)
 * 3.5.4 Debt Reports (تقارير الديون)
 * 3.5.5 Purchase Reports (تقارير الشراء)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;
    
    /**
     * Generate report based on requirements
     */
    public ReportResponse generateReport(ReportRequest request) {
        log.info("Generating report: {}", request.getReportType());
        
        try {
            ReportResponse response = switch (request.getReportType()) {
                // 3.5.1 Sales Reports
                case DAILY_SALES_SUMMARY -> generateDailySalesReport(request);
                case MONTHLY_SALES_SUMMARY -> generateMonthlySalesReport(request);
                
                // 3.5.2 Profit Reports
                case DAILY_PROFIT_SUMMARY -> generateDailyProfitReport(request);
                case MONTHLY_PROFIT_SUMMARY -> generateMonthlyProfitReport(request);
                
                // 3.5.3 Inventory Reports
                case CURRENT_INVENTORY -> generateCurrentInventoryReport(request);
                case INVENTORY_MOVEMENT -> generateInventoryMovementReport(request);
                
                // 3.5.4 Debt Reports
                case CUSTOMER_DEBT_SUMMARY -> generateCustomerDebtReport(request);
                
                // 3.5.5 Purchase Reports
                case DAILY_PURCHASE_SUMMARY -> generateDailyPurchaseReport(request);
                case MONTHLY_PURCHASE_SUMMARY -> generateMonthlyPurchaseReport(request);
            };
            
            response.setSuccess(true);
            return response;
            
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return errorResponse;
        }
    }
    
    // 3.5.1 Sales Reports Implementation
    
    /**
     * تقارير المبيعات اليومية - Daily Sales Reports
     * Requirements: إجمالي المبيعات، عدد الفواتير، المنتجات الأكثر مبيعاً
     */
    private ReportResponse generateDailySalesReport(ReportRequest request) {
        log.info("Generating daily sales report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            // Get pharmacy ID from request or use default
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get daily sales summary from database
            Map<String, Object> salesData = reportRepository.getDailySalesSummary(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            // Get best selling products
            List<Map<String, Object>> bestSellers = reportRepository.getBestSellingProducts(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            // Set summary data from database
            summary.setTotalRecords((Long) salesData.get("totalInvoices"));
            summary.setTotalAmount(((Number) salesData.get("totalSales")).doubleValue());
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Daily Sales Summary");
            summary.setReportNameAr("تقارير المبيعات اليومية");
            data.setSummary(summary);
            
            // Create details from best sellers
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (int i = 0; i < Math.min(bestSellers.size(), 10); i++) {
                Map<String, Object> product = bestSellers.get(i);
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId(String.valueOf(i + 1));
                detail.setDate(request.getStartDate().toString());
                detail.setAmount(((Number) product.get("totalRevenue")).doubleValue());
                detail.setDescription((String) product.get("productName"));
                detail.setDescriptionAr((String) product.get("productName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("quantity", product.get("totalQuantity"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.DAILY_SALES_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating daily sales report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * تقارير المبيعات الشهرية - Monthly Sales Reports
     * Requirements: إجمالي المبيعات، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    private ReportResponse generateMonthlySalesReport(ReportRequest request) {
        log.info("Generating monthly sales report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get monthly sales data
            List<Map<String, Object>> monthlyData = reportRepository.getMonthlySalesSummary(
                pharmacyId, 
                request.getStartDate().atStartOfDay(), 
                request.getEndDate().atTime(23, 59, 59));
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            // Calculate totals
            double totalSales = 0.0;
            long totalInvoices = 0;
            for (Map<String, Object> month : monthlyData) {
                totalSales += ((Number) month.get("totalSales")).doubleValue();
                totalInvoices += (Long) month.get("totalInvoices");
            }
            
            summary.setTotalRecords(totalInvoices);
            summary.setTotalAmount(totalSales);
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Monthly Sales Summary");
            summary.setReportNameAr("تقارير المبيعات الشهرية");
            data.setSummary(summary);
            
            // Create details from monthly data
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (Map<String, Object> month : monthlyData) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId(month.get("year") + "-" + month.get("month"));
                detail.setDate(month.get("year") + "-" + month.get("month"));
                detail.setAmount(((Number) month.get("totalSales")).doubleValue());
                detail.setDescription("Month " + month.get("month"));
                detail.setDescriptionAr("الشهر " + month.get("month"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("invoices", month.get("totalInvoices"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.MONTHLY_SALES_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating monthly sales report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    // 3.5.2 Profit Reports Implementation
    
    /**
     * تقارير الأرباح اليومية - Daily Profit Reports
     * Requirements: إجمالي الأرباح، نسبة الربح، المنتجات الأكثر ربحية
     */
    private ReportResponse generateDailyProfitReport(ReportRequest request) {
        log.info("Generating daily profit report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get daily profit data
            Map<String, Object> profitData = reportRepository.getDailyProfitSummary(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            // Get most profitable products
            List<Map<String, Object>> profitableProducts = reportRepository.getMostProfitableProducts(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            double totalRevenue = ((Number) profitData.get("totalRevenue")).doubleValue();
            double totalProfit = ((Number) profitData.get("totalProfit")).doubleValue();
            double profitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0;
            
            summary.setTotalRecords((Long) profitData.get("totalInvoices"));
            summary.setTotalAmount(totalProfit);
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Daily Profit Summary");
            summary.setReportNameAr("تقارير الأرباح اليومية");
            data.setSummary(summary);
            
            // Create details from profitable products
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (int i = 0; i < Math.min(profitableProducts.size(), 10); i++) {
                Map<String, Object> product = profitableProducts.get(i);
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId(String.valueOf(i + 1));
                detail.setDate(request.getStartDate().toString());
                detail.setAmount(((Number) product.get("totalProfit")).doubleValue());
                detail.setDescription((String) product.get("productName"));
                detail.setDescriptionAr((String) product.get("productName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("quantity", product.get("totalQuantity"));
                additionalData.put("profitMargin", profitMargin);
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.DAILY_PROFIT_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating daily profit report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * تقارير الأرباح الشهرية - Monthly Profit Reports
     * Requirements: إجمالي الأرباح، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    private ReportResponse generateMonthlyProfitReport(ReportRequest request) {
        log.info("Generating monthly profit report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get monthly profit data
            List<Map<String, Object>> monthlyData = reportRepository.getMonthlyProfitSummary(
                pharmacyId, 
                request.getStartDate().atStartOfDay(), 
                request.getEndDate().atTime(23, 59, 59));
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            // Calculate totals
            double totalProfit = 0.0;
            double totalRevenue = 0.0;
            for (Map<String, Object> month : monthlyData) {
                totalProfit += ((Number) month.get("totalProfit")).doubleValue();
                totalRevenue += ((Number) month.get("totalRevenue")).doubleValue();
            }
            
            summary.setTotalRecords((long) monthlyData.size());
            summary.setTotalAmount(totalProfit);
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Monthly Profit Summary");
            summary.setReportNameAr("تقارير الأرباح الشهرية");
            data.setSummary(summary);
            
            // Create details from monthly data
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (Map<String, Object> month : monthlyData) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId(month.get("year") + "-" + month.get("month"));
                detail.setDate(month.get("year") + "-" + month.get("month"));
                detail.setAmount(((Number) month.get("totalProfit")).doubleValue());
                detail.setDescription("Month " + month.get("month"));
                detail.setDescriptionAr("الشهر " + month.get("month"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("revenue", month.get("totalRevenue"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.MONTHLY_PROFIT_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating monthly profit report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    // 3.5.3 Inventory Reports Implementation
    
    /**
     * تقارير المخزون الحالي - Current Inventory Reports
     * Requirements: الكميات المتوفرة، المنتجات منخفضة المخزون، المنتجات قريبة من انتهاء الصلاحية
     */
    private ReportResponse generateCurrentInventoryReport(ReportRequest request) {
        log.info("Generating current inventory report");
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get current inventory summary
            Map<String, Object> inventoryData = reportRepository.getCurrentInventorySummary(pharmacyId);
            
            // Get low stock products
            List<Map<String, Object>> lowStockProducts = reportRepository.getLowStockProducts(pharmacyId);
            
            // Get expiring products (next 30 days)
            List<Map<String, Object>> expiringProducts = reportRepository.getExpiringProducts(
                pharmacyId, 
                request.getStartDate(), 
                request.getStartDate().plusDays(30));
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            summary.setTotalRecords((Long) inventoryData.get("totalProducts"));
            summary.setTotalAmount(((Number) inventoryData.get("totalValue")).doubleValue());
            summary.setCurrency(request.getCurrency());
            summary.setPeriod("Current Inventory");
            summary.setReportName("Current Inventory");
            summary.setReportNameAr("تقارير المخزون الحالي");
            data.setSummary(summary);
            
            // Create details from low stock and expiring products
            List<ReportResponse.DetailData> details = new ArrayList<>();
            
            // Add low stock products
            for (Map<String, Object> product : lowStockProducts) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId("low-" + product.get("productName"));
                detail.setDate("Current");
                detail.setAmount(((Number) product.get("currentQuantity")).doubleValue());
                detail.setDescription("Low Stock: " + product.get("productName"));
                detail.setDescriptionAr("مخزون منخفض: " + product.get("productName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("minStockLevel", product.get("minStockLevel"));
                additionalData.put("batchNo", product.get("batchNo"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            
            // Add expiring products
            for (Map<String, Object> product : expiringProducts) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId("exp-" + product.get("productName"));
                detail.setDate("Expiring");
                detail.setAmount(((Number) product.get("currentQuantity")).doubleValue());
                detail.setDescription("Expiring: " + product.get("productName"));
                detail.setDescriptionAr("منتهي الصلاحية: " + product.get("productName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("expiryDate", product.get("expiryDate"));
                additionalData.put("batchNo", product.get("batchNo"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.CURRENT_INVENTORY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating current inventory report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * تقارير حركة المخزون - Inventory Movement Reports
     * Requirements: المنتجات الأكثر دوراناً، المنتجات الراكدة
     */
    private ReportResponse generateInventoryMovementReport(ReportRequest request) {
        log.info("Generating inventory movement report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get inventory movement data
            List<Map<String, Object>> movementData = reportRepository.getInventoryMovement(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            // Calculate totals
            long totalProducts = movementData.size();
            double totalSold = 0.0;
            for (Map<String, Object> product : movementData) {
                totalSold += ((Number) product.get("totalSold")).doubleValue();
            }
            
            summary.setTotalRecords(totalProducts);
            summary.setTotalAmount(totalSold);
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Inventory Movement");
            summary.setReportNameAr("تقارير حركة المخزون");
            data.setSummary(summary);
            
            // Create details from movement data
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (Map<String, Object> product : movementData) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId((String) product.get("productName"));
                detail.setDate(request.getStartDate().toString());
                detail.setAmount(((Number) product.get("totalSold")).doubleValue());
                detail.setDescription((String) product.get("productName"));
                detail.setDescriptionAr((String) product.get("productName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("invoiceCount", product.get("invoiceCount"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.INVENTORY_MOVEMENT);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating inventory movement report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    // 3.5.4 Debt Reports Implementation
    
    /**
     * تقارير ديون العملاء - Customer Debt Reports
     * Requirements: إجمالي الديون، العملاء الأكثر مديونية، الديون المتأخرة
     */
    private ReportResponse generateCustomerDebtReport(ReportRequest request) {
        log.info("Generating customer debt report");
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get debt summary
            Map<String, Object> debtData = reportRepository.getCustomerDebtSummary(pharmacyId);
            
            // Get most indebted customers
            List<Map<String, Object>> indebtedCustomers = reportRepository.getMostIndebtedCustomers(pharmacyId);
            
            // Get overdue debts
            List<Map<String, Object>> overdueDebts = reportRepository.getOverdueDebts(pharmacyId);
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            summary.setTotalRecords((Long) debtData.get("totalDebts"));
            summary.setTotalAmount(((Number) debtData.get("totalDebtAmount")).doubleValue());
            summary.setCurrency(request.getCurrency());
            summary.setPeriod("Current Debt Status");
            summary.setReportName("Customer Debt Summary");
            summary.setReportNameAr("تقارير ديون العملاء");
            data.setSummary(summary);
            
            // Create details from indebted customers and overdue debts
            List<ReportResponse.DetailData> details = new ArrayList<>();
            
            // Add most indebted customers
            for (Map<String, Object> customer : indebtedCustomers) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId("customer-" + customer.get("customerName"));
                detail.setDate("Current");
                detail.setAmount(((Number) customer.get("totalDebt")).doubleValue());
                detail.setDescription((String) customer.get("customerName"));
                detail.setDescriptionAr((String) customer.get("customerName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("phone", customer.get("customerPhone"));
                additionalData.put("debtCount", customer.get("debtCount"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            
            // Add overdue debts
            for (Map<String, Object> debt : overdueDebts) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId("overdue-" + debt.get("customerName"));
                detail.setDate("Overdue");
                detail.setAmount(((Number) debt.get("debtAmount")).doubleValue());
                detail.setDescription("Overdue: " + debt.get("customerName"));
                detail.setDescriptionAr("متأخر: " + debt.get("customerName"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("phone", debt.get("customerPhone"));
                additionalData.put("dueDate", debt.get("dueDate"));
                additionalData.put("notes", debt.get("notes"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.CUSTOMER_DEBT_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating customer debt report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    // 3.5.5 Purchase Reports Implementation
    
    /**
     * تقارير الشراء اليومية - Daily Purchase Reports
     * Requirements: إجمالي قيمة عمليات الشراء
     */
    private ReportResponse generateDailyPurchaseReport(ReportRequest request) {
        log.info("Generating daily purchase report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get daily purchase data
            Map<String, Object> purchaseData = reportRepository.getDailyPurchaseSummary(
                pharmacyId, request.getStartDate(), request.getEndDate());
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            summary.setTotalRecords((Long) purchaseData.get("totalPurchases"));
            summary.setTotalAmount(((Number) purchaseData.get("totalAmount")).doubleValue());
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Daily Purchase Summary");
            summary.setReportNameAr("تقارير الشراء اليومية");
            data.setSummary(summary);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.DAILY_PURCHASE_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating daily purchase report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * تقارير الشراء الشهرية - Monthly Purchase Reports
     * Requirements: إجمالي قيمة عمليات الشراء، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    private ReportResponse generateMonthlyPurchaseReport(ReportRequest request) {
        log.info("Generating monthly purchase report for period: {} to {}", 
                request.getStartDate(), request.getEndDate());
        
        try {
            Long pharmacyId = request.getPharmacyId() != null ? 
                Long.parseLong(request.getPharmacyId()) : 1L;
            
            // Get monthly purchase data
            List<Map<String, Object>> monthlyData = reportRepository.getMonthlyPurchaseSummary(
                pharmacyId, 
                request.getStartDate().atStartOfDay(), 
                request.getEndDate().atTime(23, 59, 59));
            
            ReportResponse response = new ReportResponse();
            response.setSuccess(true);
            
            ReportResponse.ReportData data = new ReportResponse.ReportData();
            ReportResponse.SummaryData summary = new ReportResponse.SummaryData();
            
            // Calculate totals
            double totalAmount = 0.0;
            long totalPurchases = 0;
            for (Map<String, Object> month : monthlyData) {
                totalAmount += ((Number) month.get("totalAmount")).doubleValue();
                totalPurchases += (Long) month.get("totalPurchases");
            }
            
            summary.setTotalRecords(totalPurchases);
            summary.setTotalAmount(totalAmount);
            summary.setCurrency(request.getCurrency());
            summary.setPeriod(request.getStartDate() + " to " + request.getEndDate());
            summary.setReportName("Monthly Purchase Summary");
            summary.setReportNameAr("تقارير الشراء الشهرية");
            data.setSummary(summary);
            
            // Create details from monthly data
            List<ReportResponse.DetailData> details = new ArrayList<>();
            for (Map<String, Object> month : monthlyData) {
                ReportResponse.DetailData detail = new ReportResponse.DetailData();
                detail.setId(month.get("year") + "-" + month.get("month"));
                detail.setDate(month.get("year") + "-" + month.get("month"));
                detail.setAmount(((Number) month.get("totalAmount")).doubleValue());
                detail.setDescription("Month " + month.get("month"));
                detail.setDescriptionAr("الشهر " + month.get("month"));
                
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("purchases", month.get("totalPurchases"));
                detail.setAdditionalData(additionalData);
                
                details.add(detail);
            }
            data.setDetails(details);
            
            ReportResponse.ReportMetadata metadata = new ReportResponse.ReportMetadata();
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setReportType(ReportType.MONTHLY_PURCHASE_SUMMARY);
            metadata.setPharmacyId(request.getPharmacyId());
            metadata.setLanguage(request.getLanguage());
            
            response.setData(data);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating monthly purchase report: {}", e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * Create error response
     */
    private ReportResponse createErrorResponse() {
        ReportResponse errorResponse = new ReportResponse();
        errorResponse.setSuccess(false);
        return errorResponse;
    }
}
