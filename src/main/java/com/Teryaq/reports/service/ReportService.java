package com.Teryaq.reports.service;

import com.Teryaq.reports.dto.response.*;
import com.Teryaq.reports.enums.Language;
import com.Teryaq.reports.mapper.ReportMapper;
import com.Teryaq.reports.repository.ReportRepository;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.moneybox.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simplified Reports Service
 * Implements only the specific reports agreed upon with the business team:
 * 1. Monthly Purchase Report (daily breakdown)
 * 2. Daily Purchase Report
 * 3. Monthly Profit Report (daily breakdown)
 * 4. Daily Profit Report
 * 5. Most Sold Categories Monthly
 * 6. Top 10 Products Monthly
 */
@Slf4j
@Service
public class ReportService extends BaseSecurityService {
    
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final ExchangeRateService exchangeRateService;
    
    public ReportService(UserRepository userRepository, ReportRepository reportRepository, ReportMapper reportMapper, ExchangeRateService exchangeRateService) {
        super(userRepository);
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.exchangeRateService = exchangeRateService;
    }
    
    // ============================================================================
    // CURRENCY CONVERSION HELPER METHODS
    // ============================================================================
    
    /**
     * Convert amount to SYP (base currency) for consistent calculations
     */
    private BigDecimal convertToSYP(BigDecimal amount, Currency currency) {
        if (currency == Currency.SYP) {
            return amount;
        }
        return exchangeRateService.convertToSYP(amount, currency);
    }
    
    /**
     * Convert amount from SYP to target currency for display
     */
    private BigDecimal convertFromSYP(BigDecimal amount, Currency targetCurrency) {
        if (targetCurrency == Currency.SYP) {
            return amount;
        }
        Currency userCurrency = Currency.valueOf(targetCurrency.name());
        return exchangeRateService.convertFromSYP(amount, userCurrency);
    }
    
    /**
     * Process currency-aware data and convert to target currency
     */
    private Map<String, Object> processCurrencyAwareData(List<Map<String, Object>> rawData, Currency targetCurrency) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalInvoices = 0;
        BigDecimal sumForAverage = BigDecimal.ZERO;
        
        for (Map<String, Object> data : rawData) {
            Currency currency = Currency.valueOf(data.get("currency").toString());
            BigDecimal amount = new BigDecimal(data.get("totalAmount").toString());
            BigDecimal paid = new BigDecimal(data.get("totalPaid").toString());
            int invoices = ((Number) data.get("totalInvoices")).intValue();
            
            // Convert to SYP for consistent calculation
            BigDecimal amountInSYP = convertToSYP(amount, currency);
            BigDecimal paidInSYP = convertToSYP(paid, currency);
            
            totalAmount = totalAmount.add(amountInSYP);
            totalPaid = totalPaid.add(paidInSYP);
            totalInvoices += invoices;
            sumForAverage = sumForAverage.add(amountInSYP);
            
            // Handle profit and revenue if present
            if (data.containsKey("totalProfit")) {
                BigDecimal profit = new BigDecimal(data.get("totalProfit").toString());
                BigDecimal profitInSYP = convertToSYP(profit, currency);
                totalProfit = totalProfit.add(profitInSYP);
            }
            if (data.containsKey("totalRevenue")) {
                BigDecimal revenue = new BigDecimal(data.get("totalRevenue").toString());
                BigDecimal revenueInSYP = convertToSYP(revenue, currency);
                totalRevenue = totalRevenue.add(revenueInSYP);
            }
        }
        
        // Convert final totals to target currency for display
        BigDecimal finalTotalAmount = convertFromSYP(totalAmount, targetCurrency);
        BigDecimal finalTotalPaid = convertFromSYP(totalPaid, targetCurrency);
        BigDecimal finalTotalProfit = convertFromSYP(totalProfit, targetCurrency);
        BigDecimal finalTotalRevenue = convertFromSYP(totalRevenue, targetCurrency);
        BigDecimal averageAmount = totalInvoices > 0 ? convertFromSYP(sumForAverage.divide(BigDecimal.valueOf(totalInvoices), 2, BigDecimal.ROUND_HALF_UP), targetCurrency) : BigDecimal.ZERO;
        
        Map<String, Object> result;
        if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            result = Map.of(
                "totalInvoices", totalInvoices,
                "totalAmount", finalTotalAmount,
                "totalPaid", finalTotalPaid,
                "averageAmount", averageAmount,
                "totalProfit", finalTotalProfit,
                "totalRevenue", finalTotalRevenue
            );
        } else {
            result = Map.of(
                "totalInvoices", totalInvoices,
                "totalAmount", finalTotalAmount,
                "totalPaid", finalTotalPaid,
                "averageAmount", averageAmount
            );
        }
        
        return result;
    }
    
    /**
     * Process currency-aware items and convert to target currency
     */
    private List<Map<String, Object>> processCurrencyAwareItems(List<Map<String, Object>> rawItems, Currency targetCurrency) {
        return rawItems.stream()
            .map(item -> {
                Currency itemCurrency = Currency.valueOf(item.get("currency").toString());
                BigDecimal unitPrice = new BigDecimal(item.get("unitPrice").toString());
                BigDecimal subTotal = new BigDecimal(item.get("subTotal").toString());
                
                // Convert to SYP first, then to target currency
                BigDecimal unitPriceInSYP = convertToSYP(unitPrice, itemCurrency);
                BigDecimal subTotalInSYP = convertToSYP(subTotal, itemCurrency);
                
                BigDecimal finalUnitPrice = convertFromSYP(unitPriceInSYP, targetCurrency);
                BigDecimal finalSubTotal = convertFromSYP(subTotalInSYP, targetCurrency);
                
                return Map.of(
                    "productName", item.get("productName"),
                    "quantity", item.get("quantity"),
                    "unitPrice", finalUnitPrice,
                    "subTotal", finalSubTotal,
                    "supplierName", item.get("supplierName"),
                    "currency", targetCurrency.name()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Process currency-aware profit items and convert to target currency
     */
    private List<Map<String, Object>> processCurrencyAwareProfitItems(List<Map<String, Object>> rawItems, Currency targetCurrency) {
        return rawItems.stream()
            .map(item -> {
                Currency itemCurrency = Currency.valueOf(item.get("currency").toString());
                BigDecimal revenue = new BigDecimal(item.get("revenue").toString());
                BigDecimal profit = new BigDecimal(item.get("profit").toString());
                
                // Convert to SYP first, then to target currency
                BigDecimal revenueInSYP = convertToSYP(revenue, itemCurrency);
                BigDecimal profitInSYP = convertToSYP(profit, itemCurrency);
                
                BigDecimal finalRevenue = convertFromSYP(revenueInSYP, targetCurrency);
                BigDecimal finalProfit = convertFromSYP(profitInSYP, targetCurrency);
                
                return Map.of(
                    "productName", item.get("productName"),
                    "quantity", item.get("quantity"),
                    "revenue", finalRevenue,
                    "profit", finalProfit,
                    "currency", targetCurrency.name()
                );
            })
            .collect(Collectors.toList());
    }
    
    // ============================================================================
    // PURCHASE REPORTS
    // ============================================================================
    
    /**
     * Get Monthly Purchase Report with daily breakdown
     * Returns purchase data for each day in the specified month
     */
    public PurchaseReportResponse getMonthlyPurchaseReport(LocalDate startDate, LocalDate endDate, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating monthly purchase report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get daily breakdown with currency information
            List<Map<String, Object>> dailyDataRaw = reportRepository.getMonthlyPurchaseDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data with currency information
            List<Map<String, Object>> summaryRawList = reportRepository.getMonthlyPurchaseSummary(pharmacyId, startDate, endDate);
            
            // Process currency-aware data
            Map<String, Object> summaryRaw = processCurrencyAwareData(summaryRawList, currency);
            
            // Convert to DTOs using mapper
            List<PurchaseReportResponse.DailyPurchaseData> dailyData = reportMapper.toDailyPurchaseDataList(dailyDataRaw);
            PurchaseReportResponse.PurchaseSummary summary = reportMapper.toPurchaseSummary(summaryRaw);
            
            // Build response
            PurchaseReportResponse response = new PurchaseReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setDailyData(dailyData);
            response.setSummary(summary);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating monthly purchase report: {}", e.getMessage(), e);
            PurchaseReportResponse errorResponse = new PurchaseReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Daily Purchase Report
     * Returns purchase data for a specific day
     */
    public DailyPurchaseReportResponse getDailyPurchaseReport(LocalDate date, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating daily purchase report for pharmacy: {}, date: {}", pharmacyId, date);
        
        try {
            // Get daily purchase data with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getDailyPurchaseSummary(pharmacyId, date);
            
            // Process currency-aware data
            Map<String, Object> dailyDataRaw = processCurrencyAwareData(dailyDataRawList, currency);
            
            // Get purchase items for the day with currency information
            List<Map<String, Object>> purchaseItemsRaw = reportRepository.getDailyPurchaseItems(pharmacyId, date);
            
            // Process currency-aware items
            List<Map<String, Object>> processedItems = processCurrencyAwareItems(purchaseItemsRaw, currency);
            
            // Convert to DTOs using mapper
            DailyPurchaseReportResponse.DailyPurchaseData dailyData = reportMapper.toDailyPurchaseDataForDaily(dailyDataRaw);
            List<DailyPurchaseReportResponse.PurchaseItem> items = reportMapper.toDailyPurchaseItemList(processedItems);
            
            // Build response
            DailyPurchaseReportResponse response = new DailyPurchaseReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setDate(date);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setData(dailyData);
            response.setItems(items);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating daily purchase report: {}", e.getMessage(), e);
            DailyPurchaseReportResponse errorResponse = new DailyPurchaseReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    // ============================================================================
    // PROFIT REPORTS
    // ============================================================================
    
    /**
     * Get Monthly Profit Report with daily breakdown
     * Returns profit data for each day in the specified month
     */
    public ProfitReportResponse getMonthlyProfitReport(LocalDate startDate, LocalDate endDate, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating monthly profit report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get daily breakdown with currency information
            List<Map<String, Object>> dailyDataRaw = reportRepository.getMonthlyProfitDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data with currency information
            List<Map<String, Object>> summaryRawList = reportRepository.getMonthlyProfitSummary(pharmacyId, startDate, endDate);
            
            // Process currency-aware data
            Map<String, Object> summaryRaw = processCurrencyAwareData(summaryRawList, currency);
            
            // Convert to DTOs using mapper
            List<ProfitReportResponse.DailyProfitData> dailyData = reportMapper.toDailyProfitDataList(dailyDataRaw);
            ProfitReportResponse.ProfitSummary summary = reportMapper.toProfitSummary(summaryRaw);
            
            // Build response
            ProfitReportResponse response = new ProfitReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setDailyData(dailyData);
            response.setSummary(summary);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating monthly profit report: {}", e.getMessage(), e);
            ProfitReportResponse errorResponse = new ProfitReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Daily Profit Report
     * Returns profit data for a specific day
     */
    public ProfitReportResponse getDailyProfitReport(LocalDate date, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating daily profit report for pharmacy: {}, date: {}", pharmacyId, date);
        
        try {
            // Get daily profit data with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getDailyProfitSummary(pharmacyId, date);
            
            // Process currency-aware data
            Map<String, Object> dailyDataRaw = processCurrencyAwareData(dailyDataRawList, currency);
            
            // Get profit items for the day with currency information
            List<Map<String, Object>> profitItemsRaw = reportRepository.getDailyProfitItems(pharmacyId, date);
            
            // Process currency-aware profit items
            List<Map<String, Object>> processedItems = processCurrencyAwareProfitItems(profitItemsRaw, currency);
            
            // Convert to DTOs using mapper
            ProfitReportResponse.DailyProfitData dailyData = reportMapper.toDailyProfitData(dailyDataRaw);
            List<ProfitReportResponse.ProfitItem> items = reportMapper.toProfitItemList(processedItems);
            
            // Build response
            ProfitReportResponse response = new ProfitReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setDate(date);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setData(dailyData);
            response.setItems(items);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating daily profit report: {}", e.getMessage(), e);
            ProfitReportResponse errorResponse = new ProfitReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    // ============================================================================
    // CATEGORY AND PRODUCT REPORTS
    // ============================================================================
    
    /**
     * Get Most Sold Categories Monthly
     * Returns the most sold categories in the pharmacy for the specified month
     */
    public CategoryReportResponse getMostSoldCategories(LocalDate startDate, LocalDate endDate, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating most sold categories report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get most sold categories
            List<Map<String, Object>> categoriesRaw = reportRepository.getMostSoldCategories(pharmacyId, startDate, endDate);
            
            // Convert to DTOs using mapper
            List<CategoryReportResponse.CategoryData> categories = reportMapper.toCategoryDataList(categoriesRaw);
            
            // Build response
            CategoryReportResponse response = new CategoryReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setCategories(categories);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating most sold categories report: {}", e.getMessage(), e);
            CategoryReportResponse errorResponse = new CategoryReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Top 10 Products Monthly
     * Returns the top 10 most sold products in the pharmacy for the specified month
     */
    public ProductReportResponse getTop10Products(LocalDate startDate, LocalDate endDate, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating top 10 products report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get top 10 products
            List<Map<String, Object>> productsRaw = reportRepository.getTop10Products(pharmacyId, startDate, endDate);
            
            // Convert to DTOs using mapper
            List<ProductReportResponse.ProductData> products = reportMapper.toProductDataList(productsRaw);
            
            // Build response
            ProductReportResponse response = new ProductReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setProducts(products);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating top 10 products report: {}", e.getMessage(), e);
            ProductReportResponse errorResponse = new ProductReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
}
