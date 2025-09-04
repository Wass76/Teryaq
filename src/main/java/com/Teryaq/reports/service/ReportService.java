package com.Teryaq.reports.service;

import com.Teryaq.reports.dto.response.*;
import com.Teryaq.reports.enums.Currency;
import com.Teryaq.reports.enums.Language;
import com.Teryaq.reports.mapper.ReportMapper;
import com.Teryaq.reports.repository.ReportRepository;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    
    public ReportService(UserRepository userRepository, ReportRepository reportRepository, ReportMapper reportMapper) {
        super(userRepository);
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
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
            // Get daily breakdown
            List<Map<String, Object>> dailyDataRaw = reportRepository.getMonthlyPurchaseDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data
            Map<String, Object> summaryRaw = reportRepository.getMonthlyPurchaseSummary(pharmacyId, startDate, endDate);
            
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
            // Get daily purchase data
            Map<String, Object> dailyDataRaw = reportRepository.getDailyPurchaseSummary(pharmacyId, date);
            
            // Get purchase items for the day
            List<Map<String, Object>> purchaseItemsRaw = reportRepository.getDailyPurchaseItems(pharmacyId, date);
            
            // Convert to DTOs using mapper
            DailyPurchaseReportResponse.DailyPurchaseData dailyData = reportMapper.toDailyPurchaseDataForDaily(dailyDataRaw);
            List<DailyPurchaseReportResponse.PurchaseItem> items = reportMapper.toDailyPurchaseItemList(purchaseItemsRaw);
            
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
            // Get daily breakdown
            List<Map<String, Object>> dailyDataRaw = reportRepository.getMonthlyProfitDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data
            Map<String, Object> summaryRaw = reportRepository.getMonthlyProfitSummary(pharmacyId, startDate, endDate);
            
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
            // Get daily profit data
            Map<String, Object> dailyDataRaw = reportRepository.getDailyProfitSummary(pharmacyId, date);
            
            // Get profit items for the day
            List<Map<String, Object>> profitItemsRaw = reportRepository.getDailyProfitItems(pharmacyId, date);
            
            // Convert to DTOs using mapper
            ProfitReportResponse.DailyProfitData dailyData = reportMapper.toDailyProfitData(dailyDataRaw);
            List<ProfitReportResponse.ProfitItem> items = reportMapper.toProfitItemList(profitItemsRaw);
            
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
