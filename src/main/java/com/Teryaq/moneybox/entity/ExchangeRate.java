package com.Teryaq.moneybox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "from_currency", length = 3, nullable = false)
    private String fromCurrency;
    
    @Column(name = "to_currency", length = 3, nullable = false)
    private String toCurrency;
    
    @Column(name = "rate", precision = 15, scale = 6, nullable = false)
    private BigDecimal rate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
}
