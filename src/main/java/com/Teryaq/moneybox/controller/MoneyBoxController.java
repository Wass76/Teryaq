package com.Teryaq.moneybox.controller;

import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.service.MoneyBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/moneybox")
@Tag(name = "MoneyBox Management", description = "APIs for managing pharmacy cash money boxes and transactions")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin(origins = "*")
public class MoneyBoxController {
    
    private final MoneyBoxService moneyBoxService;
    
    @Operation(
        summary = "Create a new money box for the current pharmacy", 
        description = "Creates a new money box for the current user's pharmacy with initial balance and currency. " +
                     "Each pharmacy can only have one money box. The pharmacy ID is automatically extracted from the current user context."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Money box created successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or pharmacy already has a money box"),
        @ApiResponse(responseCode = "409", description = "Pharmacy already has a money box"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<MoneyBoxResponseDTO> createMoneyBox(
            @Parameter(description = "Money box creation request", required = true)
            @Valid @RequestBody MoneyBoxRequestDTO request) {
        log.info("Creating new money box for current pharmacy");
        MoneyBoxResponseDTO response = moneyBoxService.createMoneyBox(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get money box for current pharmacy", 
        description = "Retrieves the money box information for the current user's pharmacy, " +
                     "including current balance, status, and last reconciliation details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Money box retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Money box not found for the pharmacy"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<MoneyBoxResponseDTO> getMoneyBoxByCurrentPharmacy() {
        MoneyBoxResponseDTO response = moneyBoxService.getMoneyBoxByCurrentPharmacy();
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Add manual transaction to money box", 
        description = "Adds a manual transaction (income or expense) to the current pharmacy's money box. " +
                     "Positive amounts increase the balance (income), negative amounts decrease it (expense). " +
                     "This is useful for recording cash expenses, deposits, or adjustments."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid amount or money box is closed"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "409", description = "Money box is not open for transactions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/transaction")
    public ResponseEntity<MoneyBoxResponseDTO> addTransaction(
            @Parameter(description = "Transaction amount (positive for income, negative for expense)", 
                      example = "100.50", required = true)
            @NotNull @RequestParam BigDecimal amount,
            
            @Parameter(description = "Transaction description", example = "Office supplies expense")
            @RequestParam(required = false) String description) {
        log.info("Adding transaction for current pharmacy: amount={}, description={}", amount, description);
        MoneyBoxResponseDTO response = moneyBoxService.addTransaction(amount, description);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Reconcile cash with physical count", 
        description = "Reconciles the current pharmacy's money box balance with the actual physical cash count. " +
                     "If there's a discrepancy, the system balance will be adjusted to match the actual count. " +
                     "This is typically done at the end of each day or shift."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cash reconciled successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid cash count amount"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reconcile")
    public ResponseEntity<MoneyBoxResponseDTO> reconcileCash(
            @Parameter(description = "Actual physical cash count amount", 
                      example = "1250.75", required = true)
            @NotNull @DecimalMin("0.0") @RequestParam BigDecimal actualCashCount,
            
            @Parameter(description = "Reconciliation notes", example = "Daily end-of-shift count")
            @RequestParam(required = false) String notes) {
        log.info("Reconciling cash for current pharmacy: actual count={}, notes={}", actualCashCount, notes);
        MoneyBoxResponseDTO response = moneyBoxService.reconcileCash(actualCashCount, notes);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Get period summary for money box", 
        description = "Retrieves a summary of transactions for the specified period for the current pharmacy, including " +
                     "total income, total expenses, net amount, and transaction counts. " +
                     "This is useful for financial reporting and analysis."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Period summary retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxService.MoneyBoxSummary.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    public ResponseEntity<MoneyBoxService.MoneyBoxSummary> getPeriodSummary(
            @Parameter(description = "Start date and time for the period", 
                      example = "2024-01-01T00:00:00", required = true)
            @RequestParam LocalDateTime startDate,
            
            @Parameter(description = "End date and time for the period", 
                      example = "2024-01-31T23:59:59", required = true)
            @RequestParam LocalDateTime endDate) {
        log.info("Getting period summary for current pharmacy: period={} to {}", startDate, endDate);
        MoneyBoxService.MoneyBoxSummary summary = moneyBoxService.getPeriodSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
