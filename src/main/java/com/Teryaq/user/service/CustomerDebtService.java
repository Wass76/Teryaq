package com.Teryaq.user.service;

import com.Teryaq.user.dto.CustomerDebtDTORequest;
import com.Teryaq.user.dto.CustomerDebtDTOResponse;
import com.Teryaq.user.dto.PayDebtDTORequest;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.CustomerDebt;
import com.Teryaq.user.mapper.CustomerDebtMapper;
import com.Teryaq.user.repository.CustomerDebtRepository;
import com.Teryaq.user.repository.CustomerRepo;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerDebtService {

    private final CustomerDebtRepository customerDebtRepository;
    private final CustomerRepo customerRepo;
    private final CustomerDebtMapper customerDebtMapper;

    /**
     * إنشاء دين جديد للعميل
     */
    @Transactional
    public CustomerDebtDTOResponse createDebt(CustomerDebtDTORequest request) {
        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        CustomerDebt debt = customerDebtMapper.toEntity(request);
        debt.setCustomer(customer);
        
        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        return customerDebtMapper.toResponse(savedDebt);
    }

    /**
     * الحصول على ديون العميل
     */
    public List<CustomerDebtDTOResponse> getCustomerDebts(Long customerId) {
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على إجمالي ديون العميل
     */
    public BigDecimal getCustomerTotalDebt(Long customerId) {
        return customerDebtRepository.getTotalDebtByCustomerId(customerId);
    }

    /**
     * دفع الدين
     */
    @Transactional
    public CustomerDebtDTOResponse payDebt(PayDebtDTORequest request) {
        CustomerDebt debt = customerDebtRepository.findById(request.getDebtId())
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found"));

        if (debt.getStatus().equals("PAID")) {
            throw new RuntimeException("Debt is already paid");
        }

        BigDecimal newPaidAmount = debt.getPaidAmount().add(request.getPaymentAmount());
        BigDecimal newRemainingAmount = debt.getAmount().subtract(newPaidAmount);

        debt.setPaidAmount(newPaidAmount);
        debt.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            debt.setStatus("PAID");
            debt.setPaidAt(LocalDateTime.now());
        }

        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            debt.setNotes(debt.getNotes() + "\n" + request.getNotes());
        }

        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        return customerDebtMapper.toResponse(savedDebt);
    }

    /**
     * الحصول على الديون المتأخرة
     */
    public List<CustomerDebtDTOResponse> getOverdueDebts() {
        List<CustomerDebt> overdueDebts = customerDebtRepository.getOverdueDebts();
        return overdueDebts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على إجمالي الديون المتأخرة
     */
    public BigDecimal getTotalOverdueDebts() {
        return customerDebtRepository.getTotalOverdueDebts();
    }

    /**
     * حذف الدين
     */
    @Transactional
    public void deleteDebt(Long debtId) {
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found"));
        
        customerDebtRepository.delete(debt);
    }
} 