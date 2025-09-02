package com.Teryaq.reports.controller;

import com.Teryaq.reports.dto.request.ReportRequest;
import com.Teryaq.reports.dto.response.ReportResponse;
import com.Teryaq.reports.enums.ReportType;
import com.Teryaq.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for Reports API
 * Implements all 5 report categories from SRS requirements
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Reports", description = "Reports API for Pharmacy Management System")
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * Generate report based on requirements
     * Supports all 5 report categories from SRS
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate Report", description = "Generate any type of report based on SRS requirements")
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) {
        log.info("Received report generation request: {}", request.getReportType());
        
        try {
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 3.5.1 Sales Reports Endpoints
    
    /**
     * تقارير المبيعات اليومية - Daily Sales Reports
     * Requirements: إجمالي المبيعات، عدد الفواتير، المنتجات الأكثر مبيعاً
     */
    @GetMapping("/sales/daily")
    @Operation(summary = "Daily Sales Report", description = "Generate daily sales report with total sales, invoice count, and best sellers")
    public ResponseEntity<ReportResponse> getDailySalesReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating daily sales report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.DAILY_SALES_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating daily sales report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * تقارير المبيعات الشهرية - Monthly Sales Reports
     * Requirements: إجمالي المبيعات، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @GetMapping("/sales/monthly")
    @Operation(summary = "Monthly Sales Report", description = "Generate monthly sales report with comparisons and charts")
    public ResponseEntity<ReportResponse> getMonthlySalesReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating monthly sales report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.MONTHLY_SALES_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly sales report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 3.5.2 Profit Reports Endpoints
    
    /**
     * تقارير الأرباح اليومية - Daily Profit Reports
     * Requirements: إجمالي الأرباح، نسبة الربح، المنتجات الأكثر ربحية
     */
    @GetMapping("/profit/daily")
    @Operation(summary = "Daily Profit Report", description = "Generate daily profit report with profit margin and most profitable products")
    public ResponseEntity<ReportResponse> getDailyProfitReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating daily profit report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.DAILY_PROFIT_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating daily profit report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * تقارير الأرباح الشهرية - Monthly Profit Reports
     * Requirements: إجمالي الأرباح، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @GetMapping("/profit/monthly")
    @Operation(summary = "Monthly Profit Report", description = "Generate monthly profit report with comparisons and charts")
    public ResponseEntity<ReportResponse> getMonthlyProfitReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating monthly profit report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.MONTHLY_PROFIT_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly profit report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 3.5.3 Inventory Reports Endpoints
    
    /**
     * تقارير المخزون الحالي - Current Inventory Reports
     * Requirements: الكميات المتوفرة، المنتجات منخفضة المخزون، المنتجات قريبة من انتهاء الصلاحية
     */
    @GetMapping("/inventory/current")
    @Operation(summary = "Current Inventory Report", description = "Generate current inventory report with stock levels and expiring products")
    public ResponseEntity<ReportResponse> getCurrentInventoryReport(
            @RequestParam String pharmacyId,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating current inventory report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.CURRENT_INVENTORY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating current inventory report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * تقارير حركة المخزون - Inventory Movement Reports
     * Requirements: المنتجات الأكثر دوراناً، المنتجات الراكدة
     */
    @GetMapping("/inventory/movement")
    @Operation(summary = "Inventory Movement Report", description = "Generate inventory movement report with fast and slow moving products")
    public ResponseEntity<ReportResponse> getInventoryMovementReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating inventory movement report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.INVENTORY_MOVEMENT)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating inventory movement report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 3.5.4 Debt Reports Endpoints
    
    /**
     * تقارير ديون العملاء - Customer Debt Reports
     * Requirements: إجمالي الديون، العملاء الأكثر مديونية، الديون المتأخرة
     */
    @GetMapping("/debt/summary")
    @Operation(summary = "Customer Debt Report", description = "Generate customer debt report with total debts and overdue amounts")
    public ResponseEntity<ReportResponse> getCustomerDebtReport(
            @RequestParam String pharmacyId,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating customer debt report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.CUSTOMER_DEBT_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating customer debt report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 3.5.5 Purchase Reports Endpoints
    
    /**
     * تقارير الشراء اليومية - Daily Purchase Reports
     * Requirements: إجمالي قيمة عمليات الشراء
     */
    @GetMapping("/purchase/daily")
    @Operation(summary = "Daily Purchase Report", description = "Generate daily purchase report with total purchase value")
    public ResponseEntity<ReportResponse> getDailyPurchaseReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating daily purchase report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.DAILY_PURCHASE_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating daily purchase report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * تقارير الشراء الشهرية - Monthly Purchase Reports
     * Requirements: إجمالي قيمة عمليات الشراء، مقارنة مع الأشهر السابقة، رسوم بيانية
     */
    @GetMapping("/purchase/monthly")
    @Operation(summary = "Monthly Purchase Report", description = "Generate monthly purchase report with comparisons and charts")
    public ResponseEntity<ReportResponse> getMonthlyPurchaseReport(
            @RequestParam String pharmacyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "SYP") String currency,
            @RequestParam(defaultValue = "EN") String language) {
        
        log.info("Generating monthly purchase report for pharmacy: {}", pharmacyId);
        
        try {
            ReportRequest request = ReportRequest.builder()
                    .reportType(ReportType.MONTHLY_PURCHASE_SUMMARY)
                    .pharmacyId(pharmacyId)
                    .startDate(LocalDate.parse(startDate))
                    .endDate(LocalDate.parse(endDate))
                    .currency(currency)
                    .language(language)
                    .build();
            
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly purchase report: {}", e.getMessage(), e);
            ReportResponse errorResponse = new ReportResponse();
            errorResponse.setSuccess(false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the reports service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Reports service is running");
    }
}
