package com.Teryaq.reports.controller;

import com.Teryaq.reports.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller for Reports Database Queries
 * Used to test database queries independently
 */
@RestController
@RequestMapping("/api/v1/reports/test")
@CrossOrigin("*")
public class ReportTestController {

    @Autowired
    private ReportRepository reportRepository;

    /**
     * Test all reports status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestParam(defaultValue = "1") Long pharmacyId) {
        Map<String, Object> status = new HashMap<>();
        status.put("message", "Reports test endpoints are working");
        status.put("pharmacyId", pharmacyId);
        status.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(status);
    }

    // 3.5.1 Sales Reports Test Endpoints

    /**
     * Test daily sales summary
     */
    @GetMapping("/sales/daily")
    public ResponseEntity<Map<String, Object>> testDailySales(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            Map<String, Object> result = reportRepository.getDailySalesSummary(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test best selling products
     */
    @GetMapping("/sales/best-sellers")
    public ResponseEntity<List<Map<String, Object>>> testBestSellers(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getBestSellingProducts(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // 3.5.2 Profit Reports Test Endpoints

    /**
     * Test daily profit summary
     */
    @GetMapping("/profit/daily")
    public ResponseEntity<Map<String, Object>> testDailyProfit(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            Map<String, Object> result = reportRepository.getDailyProfitSummary(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test most profitable products
     */
    @GetMapping("/profit/most-profitable")
    public ResponseEntity<List<Map<String, Object>>> testMostProfitable(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getMostProfitableProducts(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // 3.5.3 Inventory Reports Test Endpoints

    /**
     * Test current inventory summary
     */
    @GetMapping("/inventory/current")
    public ResponseEntity<Map<String, Object>> testCurrentInventory(
            @RequestParam(defaultValue = "1") Long pharmacyId) {
        
        try {
            Map<String, Object> result = reportRepository.getCurrentInventorySummary(pharmacyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test low stock products
     */
    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Map<String, Object>>> testLowStock(
            @RequestParam(defaultValue = "1") Long pharmacyId) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getLowStockProducts(pharmacyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Test expiring products
     */
    @GetMapping("/inventory/expiring")
    public ResponseEntity<List<Map<String, Object>>> testExpiring(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-12-31") String endDate) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getExpiringProducts(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Test inventory movement
     */
    @GetMapping("/inventory/movement")
    public ResponseEntity<List<Map<String, Object>>> testInventoryMovement(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getInventoryMovement(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // 3.5.4 Debt Reports Test Endpoints

    /**
     * Test customer debt summary
     */
    @GetMapping("/debt/summary")
    public ResponseEntity<Map<String, Object>> testDebtSummary(
            @RequestParam(defaultValue = "1") Long pharmacyId) {
        
        try {
            Map<String, Object> result = reportRepository.getCustomerDebtSummary(pharmacyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test most indebted customers
     */
    @GetMapping("/debt/most-indebted")
    public ResponseEntity<List<Map<String, Object>>> testMostIndebted(
            @RequestParam(defaultValue = "1") Long pharmacyId) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getMostIndebtedCustomers(pharmacyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * Test overdue debts
     */
    @GetMapping("/debt/overdue")
    public ResponseEntity<List<Map<String, Object>>> testOverdueDebts(
            @RequestParam(defaultValue = "1") Long pharmacyId) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getOverdueDebts(pharmacyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    // 3.5.5 Purchase Reports Test Endpoints

    /**
     * Test daily purchase summary
     */
    @GetMapping("/purchase/daily")
    public ResponseEntity<Map<String, Object>> testDailyPurchase(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            Map<String, Object> result = reportRepository.getDailyPurchaseSummary(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test purchase by supplier
     */
    @GetMapping("/purchase/by-supplier")
    public ResponseEntity<List<Map<String, Object>>> testPurchaseBySupplier(
            @RequestParam(defaultValue = "1") Long pharmacyId,
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2024-01-31") String endDate) {
        
        try {
            List<Map<String, Object>> result = reportRepository.getPurchaseBySupplier(
                pharmacyId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }
}
