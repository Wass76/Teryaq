package com.Teryaq.moneybox.controller;

import com.Teryaq.moneybox.dto.*;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.Enum.TransactionType;
import com.Teryaq.moneybox.service.MoneyBoxService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/money-box")
@RequiredArgsConstructor
@Tag(name = "Money Box Management", description = "APIs for managing pharmacy cash operations")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class MoneyBoxController {
    
    private final MoneyBoxService moneyBoxService;
    
    @PostMapping("/open")
    @Operation(summary = "Open money box", description = "Open a new money box for the day")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Money box opened successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Money box already open for today"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MoneyBoxResponseDTO> openMoneyBox(@Valid @RequestBody MoneyBoxRequestDTO request) {
        MoneyBox moneyBox = moneyBoxService.openMoneyBox(
            request.getOpeningBalance(), 
            request.getNotes()
        );
        
        MoneyBoxResponseDTO response = mapToResponseDTO(moneyBox);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/close")
    @Operation(summary = "Close money box", description = "Close the money box and reconcile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Money box closed successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MoneyBoxResponseDTO> closeMoneyBox(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Actual cash balance", example = "50000") @RequestParam BigDecimal actualBalance,
            @Parameter(description = "Closing notes", example = "End of day reconciliation") @RequestParam String notes) {
        
        MoneyBox moneyBox = moneyBoxService.closeMoneyBox(id, actualBalance, notes);
        MoneyBoxResponseDTO response = mapToResponseDTO(moneyBox);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/summary")
    @Operation(summary = "Get money box summary", description = "Get current status and totals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxSummary.class))),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MoneyBoxSummary> getMoneyBoxSummary(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id) {
        MoneyBoxSummary summary = moneyBoxService.getMoneyBoxSummary(id);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/pharmacy/current")
    @Operation(summary = "Get current money box", description = "Get currently open money box for pharmacy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current money box retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MoneyBoxResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "No open money box found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MoneyBoxResponseDTO> getCurrentMoneyBox() {

        
        Optional<MoneyBox> moneyBox = moneyBoxService.getCurrentMoneyBox();
        
        if (moneyBox.isPresent()) {
            MoneyBoxResponseDTO response = mapToResponseDTO(moneyBox.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/transactions")
    @Operation(summary = "Add transaction", description = "Add a new transaction to the money box")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponseDTO> addTransaction(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO request) {
        
        TransactionType type = TransactionType.valueOf(request.getTransactionType());
        MoneyBoxTransaction transaction = moneyBoxService.addTransaction(
            id, type, request.getAmount(), request.getCurrency(), request.getDescription()
        );
        
        TransactionResponseDTO response = mapToTransactionResponseDTO(transaction);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/withdrawal")
    @Operation(summary = "Add withdrawal transaction", description = "Record cash taken from money box")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal recorded successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponseDTO> addWithdrawal(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Withdrawal amount", example = "10000") @RequestParam BigDecimal amount,
            @Parameter(description = "Currency", example = "SYP") @RequestParam String currency,
            @Parameter(description = "Reason for withdrawal", example = "Bank deposit") @RequestParam String reason,
            @Parameter(description = "Receipt number", example = "RCP-001") @RequestParam String receiptNumber) {
        
        MoneyBoxTransaction transaction = moneyBoxService.addWithdrawalTransaction(id, amount, currency, reason, receiptNumber);
        TransactionResponseDTO response = mapToTransactionResponseDTO(transaction);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/deposit")
    @Operation(summary = "Add deposit transaction", description = "Record cash added to money box")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit recorded successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponseDTO> addDeposit(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Deposit amount", example = "50000") @RequestParam BigDecimal amount,
            @Parameter(description = "Currency", example = "SYP") @RequestParam String currency,
            @Parameter(description = "Reason for deposit", example = "Bank withdrawal") @RequestParam String reason,
            @Parameter(description = "Receipt number", example = "RCP-002") @RequestParam String receiptNumber) {
        
        MoneyBoxTransaction transaction = moneyBoxService.addDepositTransaction(id, amount, currency, reason, receiptNumber);
        TransactionResponseDTO response = mapToTransactionResponseDTO(transaction);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get transactions", description = "Get all transactions for money box")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @Parameter(description = "Money box ID", example = "1") @PathVariable Long id) {
        // TODO: Implement transaction retrieval
        return ResponseEntity.ok(new ArrayList<>());
    }
    
    private MoneyBoxResponseDTO mapToResponseDTO(MoneyBox moneyBox) {
        return MoneyBoxResponseDTO.builder()
            .id(moneyBox.getId())
            .pharmacyId(moneyBox.getPharmacyId())
            .businessDate(moneyBox.getBusinessDate())
            .status(moneyBox.getStatus().name())
            .periodType(moneyBox.getPeriodType().name())
            .openingBalance(moneyBox.getOpeningBalance())
            .currentBalance(moneyBox.getOpeningBalance().add(moneyBox.getTotalCashIn()).subtract(moneyBox.getTotalCashOut()))
            .totalCashIn(moneyBox.getTotalCashIn())
            .totalCashOut(moneyBox.getTotalCashOut())
            .netCashFlow(moneyBox.getNetCashFlow())
            .openedAt(moneyBox.getOpenedAt())
            .openedBy("Employee " + moneyBox.getOpenedBy()) // TODO: Get actual employee name
            .notes(moneyBox.getOpeningNotes())
            .build();
    }
    
    private TransactionResponseDTO mapToTransactionResponseDTO(MoneyBoxTransaction transaction) {
        return TransactionResponseDTO.builder()
            .id(transaction.getId())
            .moneyBoxId(transaction.getMoneyBoxId())
            .transactionType(transaction.getTransactionType().name())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .amountInSYP(transaction.getAmountInSYP())
            .description(transaction.getDescription())
            .referenceType(transaction.getReferenceType())
            .referenceNumber(transaction.getReferenceNumber())
            .status(transaction.getStatus())
            .transactionDate(transaction.getTransactionDate())
            .employeeName("Employee " + transaction.getEmployeeId()) // TODO: Get actual employee name
            .build();
    }
}
