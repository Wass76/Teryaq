package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.CustomerDebtDTORequest;
import com.Teryaq.user.dto.CustomerDebtDTOResponse;
import com.Teryaq.user.entity.CustomerDebt;
import org.springframework.stereotype.Component;

@Component
public class CustomerDebtMapper {

    public CustomerDebt toEntity(CustomerDebtDTORequest dto) {
        return CustomerDebt.builder()
                .amount(dto.getAmount())
                .paidAmount(0f)
                .remainingAmount(dto.getAmount())
                .dueDate(dto.getDueDate())
                .notes(dto.getNotes())
                .status("ACTIVE")
                .build();
    }

    public CustomerDebtDTOResponse toResponse(CustomerDebt debt) {
        return CustomerDebtDTOResponse.builder()
                .id(debt.getId())
                .customerId(debt.getCustomer().getId())
                .customerName(debt.getCustomer().getName())
                .amount(debt.getAmount())
                .paidAmount(debt.getPaidAmount())
                .remainingAmount(debt.getRemainingAmount())
                .dueDate(debt.getDueDate())
                .notes(debt.getNotes())
                .status(debt.getStatus())
                .createdAt(debt.getCreatedAt())
                .paidAt(debt.getPaidAt())
                .build();
    }
} 