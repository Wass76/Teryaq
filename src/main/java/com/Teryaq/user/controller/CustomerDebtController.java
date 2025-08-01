package com.Teryaq.user.controller;

import com.Teryaq.user.dto.CustomerDebtDTORequest;
import com.Teryaq.user.dto.CustomerDebtDTOResponse;
import com.Teryaq.user.dto.PayDebtDTORequest;
import com.Teryaq.user.service.CustomerDebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customer-debts")
@Tag(name = "Customer Debt Management", description = "APIs for managing customer debts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerDebtController {

    private final CustomerDebtService customerDebtService;

    /**
     * إنشاء دين جديد للعميل
     */
    @PostMapping
    @Operation(summary = "Create new customer debt", description = "Creates a new debt for a customer")
    public ResponseEntity<CustomerDebtDTOResponse> createDebt(@RequestBody CustomerDebtDTORequest request) {
        CustomerDebtDTOResponse response = customerDebtService.createDebt(request);
        return ResponseEntity.ok(response);
    }

    /**
     * الحصول على ديون العميل
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer debts", description = "Returns all debts for a specific customer")
    public ResponseEntity<List<CustomerDebtDTOResponse>> getCustomerDebts(@PathVariable Long customerId) {
        List<CustomerDebtDTOResponse> debts = customerDebtService.getCustomerDebts(customerId);
        return ResponseEntity.ok(debts);
    }

    /**
     * الحصول على إجمالي ديون العميل
     */
    @GetMapping("/customer/{customerId}/total")
    @Operation(summary = "Get customer total debt", description = "Returns the total debt amount for a customer")
    public ResponseEntity<BigDecimal> getCustomerTotalDebt(@PathVariable Long customerId) {
        BigDecimal totalDebt = customerDebtService.getCustomerTotalDebt(customerId);
        return ResponseEntity.ok(totalDebt);
    }

    /**
     * دفع الدين
     */
    @PostMapping("/pay")
    @Operation(summary = "Pay debt", description = "Makes a payment towards a debt")
    public ResponseEntity<CustomerDebtDTOResponse> payDebt(@RequestBody PayDebtDTORequest request) {
        CustomerDebtDTOResponse response = customerDebtService.payDebt(request);
        return ResponseEntity.ok(response);
    }

    /**
     * الحصول على الديون المتأخرة
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue debts", description = "Returns all overdue debts")
    public ResponseEntity<List<CustomerDebtDTOResponse>> getOverdueDebts() {
        List<CustomerDebtDTOResponse> overdueDebts = customerDebtService.getOverdueDebts();
        return ResponseEntity.ok(overdueDebts);
    }

    /**
     * الحصول على إجمالي الديون المتأخرة
     */
    @GetMapping("/overdue/total")
    @Operation(summary = "Get total overdue debts", description = "Returns the total amount of overdue debts")
    public ResponseEntity<BigDecimal> getTotalOverdueDebts() {
        BigDecimal totalOverdue = customerDebtService.getTotalOverdueDebts();
        return ResponseEntity.ok(totalOverdue);
    }

    /**
     * حذف الدين
     */
    @DeleteMapping("/{debtId}")
    @Operation(summary = "Delete debt", description = "Deletes a debt record")
    public ResponseEntity<Void> deleteDebt(@PathVariable Long debtId) {
        customerDebtService.deleteDebt(debtId);
        return ResponseEntity.ok().build();
    }
} 