package com.Teryaq.moneybox.entity;

import com.Teryaq.moneybox.Enum.TransactionType;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_box_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MoneyBoxTransaction extends AuditedEntity {
    
    @Column(nullable = false)
    private Long moneyBoxId;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency; // SYP, USD, EUR
    
    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate; // Rate to SYP
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInSYP; // Always stored in SYP
    
    // References (only for entity-based transactions)
    @Column
    private Long referenceId; // ID of related entity (nullable)
    
    @Column
    private String referenceType; // SALE_INVOICE, PURCHASE_INVOICE, CUSTOMER_RETURN, CASH_MOVEMENT
    
    @Column
    private String referenceNumber; // Human-readable reference
    
    // Parties Involved
    @Column
    private Long customerId; // For customer transactions
    
    @Column
    private Long supplierId; // For supplier transactions
    
    @Column(nullable = false)
    private Long employeeId; // Who performed transaction
    
    @Column
    private Long authorizedBy; // Manager approval if needed
    
    // Business Context
    @Column
    private String description;
    
    @Column
    private String notes;
    
    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, CANCELLED, REVERSED
    
    // Audit & Control
    @Column
    private String paymentMethod; // CASH, CARD, BANK_TRANSFER, CHECK
    
    @Column
    private String receiptNumber; // Physical receipt number
    
    @Column(nullable = false)
    private Boolean requiresApproval = false;
    
    @Column
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    
    @Override
    protected String getSequenceName() {
        return "money_box_transaction_id_seq";
    }
}
