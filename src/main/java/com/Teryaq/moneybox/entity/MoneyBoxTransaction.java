package com.Teryaq.moneybox.entity;

import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.user.Enum.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_box_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "money_box_id", nullable = false)
    private MoneyBox moneyBox;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "reference_type")
    private String referenceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "original_currency", length = 3)
    private Currency originalCurrency;
    
    @Column(name = "original_amount", precision = 15, scale = 2)
    private BigDecimal originalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "converted_currency", length = 3)
    private Currency convertedCurrency;
    
    @Column(name = "converted_amount", precision = 15, scale = 2)
    private BigDecimal convertedAmount;
    
    @Column(name = "exchange_rate", precision = 15, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(name = "conversion_timestamp")
    private LocalDateTime conversionTimestamp;
    
    @Column(name = "conversion_source")
    private String conversionSource;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
}
