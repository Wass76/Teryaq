package com.Teryaq.moneybox.entity;

import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import com.Teryaq.moneybox.Enum.PeriodType;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_boxes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MoneyBox extends AuditedEntity {
    
    @Column(nullable = false)
    private Long pharmacyId;
    
    @Column(nullable = false)
    private LocalDate businessDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MoneyBoxStatus status; // OPEN, CLOSED, RECONCILED
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Column
    private Long parentMoneyBoxId; // For aggregation (daily → weekly → monthly)
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal closingBalance;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal expectedBalance;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal actualBalance;
    
    @Column
    private LocalDateTime openedAt;
    
    @Column
    private LocalDateTime closedAt;
    
    @Column
    private Long openedBy; // Employee ID
    
    @Column
    private Long closedBy; // Employee ID
    
    @Column
    private String openingNotes;
    
    @Column
    private String closingNotes;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalCashIn;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalCashOut;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal netCashFlow;
    
    @Override
    protected String getSequenceName() {
        return "money_box_id_seq";
    }
}
