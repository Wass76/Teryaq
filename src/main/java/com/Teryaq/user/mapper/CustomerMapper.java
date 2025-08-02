package com.Teryaq.user.mapper;

import org.springframework.stereotype.Component;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.dto.CustomerDebtDTOResponse;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.CustomerDebt;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    public CustomerDTOResponse toResponse(Customer customer) {
        if (customer == null) return null;
        
        CustomerDTOResponse response = CustomerDTOResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .build();

        // حساب إجمالي الديون والمبالغ المدفوعة
        if (customer.getDebts() != null && !customer.getDebts().isEmpty()) {
            Float totalDebt = customer.getDebts().stream()
                    .map(CustomerDebt::getAmount)
                    .reduce(0f, Float::sum);
            
            Float totalPaid = customer.getDebts().stream()
                    .map(CustomerDebt::getPaidAmount)
                    .reduce(0f, Float::sum);
            
            int activeDebtsCount = (int) customer.getDebts().stream()
                    .filter(debt -> "ACTIVE".equals(debt.getStatus()))
                    .count();

            response.setTotalDebt(totalDebt);
            response.setTotalPaid(totalPaid);
            response.setRemainingDebt(totalDebt - totalPaid);
            response.setActiveDebtsCount(activeDebtsCount);
            
            // تحويل الديون إلى DTOs
            List<CustomerDebtDTOResponse> debtDtos = customer.getDebts().stream()
                    .map(this::toDebtResponse)
                    .collect(Collectors.toList());
            response.setDebts(debtDtos);
        } else {
            response.setTotalDebt(0.0f);
            response.setTotalPaid(0.0f);
            response.setRemainingDebt(0.0f);
            response.setActiveDebtsCount(0);
        }

        return response;
    }

    public CustomerDebtDTOResponse toDebtResponse(CustomerDebt debt) {
        if (debt == null) return null;
        
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

    public Customer toEntity(CustomerDTORequest dto) {
        if (dto == null) return null;
        
        // إذا الاسم فارغ أو null، عيّن القيمة الافتراضية
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        
        return customer;
    }

    public void updateEntityFromDto(Customer customer, CustomerDTORequest dto) {
        if (dto == null || customer == null) return;
        
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        customer.setName(name);
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
    }
}
