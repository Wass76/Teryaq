package com.Teryaq.user.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_debt")
public class CustomerDebt extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal amount; // مبلغ الدين

    @Column(nullable = false)
    private BigDecimal paidAmount; // المبلغ المدفوع

    @Column(nullable = false)
    private BigDecimal remainingAmount; // المبلغ المتبقي

    @Column(nullable = false)
    private LocalDateTime dueDate; // تاريخ استحقاق الدين

    @Column
    private String notes; // ملاحظات

    @Column(nullable = false)
    private String status; // ACTIVE, PAID, OVERDUE

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime paidAt; // تاريخ الدفع

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @Override
    protected String getSequenceName() {
        return "customer_debt_id_seq";
    }
} 