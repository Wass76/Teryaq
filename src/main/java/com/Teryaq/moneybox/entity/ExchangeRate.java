package com.Teryaq.moneybox.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExchangeRate extends AuditedEntity {
    
    @Column(nullable = false, length = 3)
    private String fromCurrency;
    
    @Column(nullable = false, length = 3)
    private String toCurrency;
    
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;
    
    @Column(nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column
    private LocalDateTime effectiveTo;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column
    private String source; // "MANUAL", "API", "SYSTEM"
    
    @Column
    private String notes;
    
    @Override
    protected String getSequenceName() {
        return "exchange_rate_id_seq";
    }
}
