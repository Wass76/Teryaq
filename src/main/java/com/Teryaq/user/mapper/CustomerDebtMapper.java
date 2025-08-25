package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.CustomerDebtDTORequest;
import com.Teryaq.user.dto.CustomerDebtDTOResponse;
import com.Teryaq.user.entity.CustomerDebt;
import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
                .paymentMethod(dto.getPaymentMethod())
                .build();
    }

    public CustomerDebtDTOResponse toResponse(CustomerDebt debt) {
        return CustomerDebtDTOResponse.builder()
                .id(debt.getId())
                .customerId(debt.getCustomer().getId())
                .customerName(debt.getCustomer().getName())
                .pharmacyId(debt.getCustomer().getPharmacy().getId())
                .amount(debt.getAmount())
                .paidAmount(debt.getPaidAmount())
                .remainingAmount(debt.getRemainingAmount())
                .dueDate(debt.getDueDate())
                .notes(debt.getNotes())
                .status(debt.getStatus())
                .createdAt(debt.getCreatedAt())
                .paidAt(debt.getPaidAt())
                .paymentMethod(debt.getPaymentMethod())
                .build();
    }
    
    public CustomerDebt toEntityFromSaleInvoice(SaleInvoiceDTORequest request, Float remainingAmount, LocalDate dueDate) {
        return CustomerDebt.builder()
                .amount(remainingAmount)
                .paidAmount(0f)
                .remainingAmount(remainingAmount)
                .dueDate(dueDate)
                .notes("Debt from sale invoice")
                .status("ACTIVE")
                .paymentMethod(request.getPaymentMethod())
                .build();
    }
} 