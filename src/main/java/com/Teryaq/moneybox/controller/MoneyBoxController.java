package com.Teryaq.moneybox.controller;

import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Teryaq.moneybox.dto.CurrencyConversionResponseDTO;
import com.Teryaq.moneybox.dto.ExchangeRateResponseDTO;
import com.Teryaq.moneybox.service.MoneyBoxService;
import com.Teryaq.moneybox.service.ExchangeRateService;
import com.Teryaq.user.Enum.Currency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moneybox")
@RequiredArgsConstructor
@Tag(name = "Money Box Management", description = "APIs for managing pharmacy money box with multi-currency support")
@CrossOrigin(origins = "*")
public class MoneyBoxController {
    
    private final MoneyBoxService moneyBoxService;
    private final ExchangeRateService exchangeRateService;
    
    @PostMapping
    @Operation(summary = "Create a new money box", description = "Creates a new money box for the current pharmacy with automatic currency conversion to SYP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Money box created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Pharmacy already has a money box"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> createMoneyBox(@Valid @RequestBody MoneyBoxRequestDTO request) {
        MoneyBoxResponseDTO response = moneyBoxService.createMoneyBox(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get current pharmacy money box", description = "Retrieves the money box information for the current pharmacy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Money box retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> getMoneyBox() {
        MoneyBoxResponseDTO response = moneyBoxService.getMoneyBoxByCurrentPharmacy();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/transactions")
    @Operation(summary = "Add manual transaction", description = "Adds a manual transaction to the money box with automatic currency conversion to SYP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> addTransaction(
            @Parameter(description = "Transaction amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Transaction description") @RequestParam String description) {
        
        MoneyBoxResponseDTO response = moneyBoxService.addTransaction(amount, description);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/transactions/syp")
    @Operation(summary = "Add transaction in SYP", description = "Adds a manual transaction to the money box in SYP (legacy endpoint)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> addTransactionInSYP(
            @Parameter(description = "Transaction amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Transaction description") @RequestParam String description) {
        
        MoneyBoxResponseDTO response = moneyBoxService.addTransaction(amount, description);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reconcile")
    @Operation(summary = "Reconcile cash", description = "Reconciles the money box with actual cash count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cash reconciled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reconciliation data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> reconcileCash(
            @Parameter(description = "Actual cash count") @RequestParam BigDecimal actualCashCount,
            @Parameter(description = "Reconciliation notes") @RequestParam(required = false) String notes) {
        
        MoneyBoxResponseDTO response = moneyBoxService.reconcileCash(actualCashCount, notes);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "Get period summary", description = "Gets money box summary for a specific time period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxService.MoneyBoxSummary> getPeriodSummary(
            @Parameter(description = "Start date (ISO format)") @RequestParam String startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam String endDate) {
        
        // Implementation would parse the date strings and call the service
        // For now, return a placeholder response
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/currency/convert")
    @Operation(summary = "Convert currency to SYP", description = "Converts an amount from any currency to SYP using current exchange rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Currency converted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid conversion request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CurrencyConversionResponseDTO> convertToSYP(
            @Parameter(description = "Amount to convert") @RequestParam BigDecimal amount,
            @Parameter(description = "Source currency") @RequestParam Currency fromCurrency) {
        
        CurrencyConversionResponseDTO response = moneyBoxService.convertCurrencyToSYP(amount, fromCurrency);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/currency/rates")
    @Operation(summary = "Get current exchange rates", description = "Gets current exchange rates for all supported currencies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rates retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ExchangeRateResponseDTO>> getCurrentRates() {
        List<ExchangeRateResponseDTO> rates = moneyBoxService.getCurrentExchangeRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all money box transactions", description = "Retrieves all transactions for the current pharmacy money box with dual currency amounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<MoneyBoxTransactionResponseDTO>> getAllTransactions(
            @Parameter(description = "Start date (ISO format, optional)",example = "2024-01-01T00:00:00") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format, optional)",example = "2024-01-01T00:00:00") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Transaction type (optional)") @RequestParam(required = false) String transactionType) {
        
        List<MoneyBoxTransactionResponseDTO> transactions = moneyBoxService.getAllTransactions(startDate, endDate, transactionType);
        return ResponseEntity.ok(transactions);
    }

    // TODO: Implement currency conversion report endpoint
    // @GetMapping("/reports/currency-conversion")
    // @Operation(summary = "Get currency conversion report", description = "Shows all invoices with their dual currency amounts and conversion details")
    // public ResponseEntity<CurrencyConversionReportResponse> getCurrencyConversionReport(
    //     @Parameter(description = "Start date (ISO format)") @RequestParam String startDate,
    //     @Parameter(description = "End date (ISO format)") @RequestParam String endDate,
    //     @Parameter(description = "Currency filter (optional)") @RequestParam(required = false) Currency currency) {
    //     // Implementation will show all invoices with dual currency amounts
    //     // Including purchase invoices, sale invoices with their original and SYP equivalent amounts
    //     // Plus exchange rates used and conversion timestamps
    // }
}
