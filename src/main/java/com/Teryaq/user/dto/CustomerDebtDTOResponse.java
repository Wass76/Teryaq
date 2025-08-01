package com.Teryaq.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDebtDTOResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime dueDate;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
} 