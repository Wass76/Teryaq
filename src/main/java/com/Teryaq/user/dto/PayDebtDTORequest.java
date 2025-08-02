package com.Teryaq.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayDebtDTORequest {
    private Long debtId;
    private Float paymentAmount;
    private String notes;
} 