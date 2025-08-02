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
import com.Teryaq.utils.exception.ConflictException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
        validateDebtRequest(request);
        
        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        CustomerDebt debt = customerDebtMapper.toEntity(request);
        debt.setCustomer(customer);
        debt.setPaidAmount(0.0f);
        debt.setRemainingAmount(request.getAmount());
        debt.setStatus("ACTIVE");
        
        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        return customerDebtMapper.toResponse(savedDebt);
    }

    /**
     * الحصول على ديون العميل
     */
    public List<CustomerDebtDTOResponse> getCustomerDebts(Long customerId) {
        validateCustomerExists(customerId);
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على ديون العميل حسب الحالة
     */
    public List<CustomerDebtDTOResponse> getCustomerDebtsByStatus(Long customerId, String status) {
        validateCustomerExists(customerId);
        validateStatus(status);
        
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على دين محدد
     */
    public CustomerDebtDTOResponse getDebtById(Long debtId) {
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));
        return customerDebtMapper.toResponse(debt);
    }

    /**
     * الحصول على إجمالي ديون العميل
     */
    public Float getCustomerTotalDebt(Long customerId) {
        validateCustomerExists(customerId);
        return customerDebtRepository.getTotalDebtByCustomerId(customerId).floatValue();
    }

    /**
     * دفع الدين
     */
    @Transactional
    public CustomerDebtDTOResponse payDebt(PayDebtDTORequest request) {
        validatePaymentRequest(request);
        
        CustomerDebt debt = customerDebtRepository.findById(request.getDebtId())
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + request.getDebtId()));

        if ("PAID".equals(debt.getStatus())) {
            throw new ConflictException("Debt is already paid");
        }

        Float paymentAmount = request.getPaymentAmount().floatValue();
        Float newPaidAmount = debt.getPaidAmount() + paymentAmount;
        Float newRemainingAmount = debt.getAmount() - newPaidAmount;

        // التحقق من أن المبلغ المدفوع لا يتجاوز الدين
        if (newPaidAmount > debt.getAmount()) {
            throw new ConflictException("Payment amount cannot exceed debt amount");
        }

        debt.setPaidAmount(newPaidAmount);
        debt.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount <= 0) {
            debt.setStatus("PAID");
            debt.setPaidAt(LocalDate.now());
        } else if (debt.getDueDate().isBefore(LocalDate.now())) {
            debt.setStatus("OVERDUE");
        }

        // إضافة ملاحظات الدفع
        if (StringUtils.hasText(request.getNotes())) {
            String paymentNote = "Payment: " + paymentAmount + " - " + request.getNotes();
            debt.setNotes(debt.getNotes() != null ? debt.getNotes() + "\n" + paymentNote : paymentNote);
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
    public Float getTotalOverdueDebts() {
        return customerDebtRepository.getTotalOverdueDebts().floatValue();
    }

    /**
     * الحصول على الديون حسب الحالة
     */
    public List<CustomerDebtDTOResponse> getDebtsByStatus(String status) {
        validateStatus(status);
        List<CustomerDebt> debts = customerDebtRepository.findByStatusOrderByCreatedAtDesc(status);
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على الديون ضمن نطاق تاريخي
     */
    public List<CustomerDebtDTOResponse> getDebtsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<CustomerDebt> allDebts = customerDebtRepository.findAll();
        return allDebts.stream()
                .filter(debt -> debt.getCreatedAt().toLocalDate().isAfter(startDate) && debt.getCreatedAt().toLocalDate().isBefore(endDate))
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على الديون ضمن نطاق مالي
     */
    public List<CustomerDebtDTOResponse> getDebtsByAmountRange(Float minAmount, Float maxAmount) {
        List<CustomerDebt> allDebts = customerDebtRepository.findAll();
        return allDebts.stream()
                .filter(debt -> debt.getAmount() >= minAmount && debt.getAmount() <= maxAmount)
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * تحديث حالة الدين
     */
    @Transactional
    public CustomerDebtDTOResponse updateDebtStatus(Long debtId, String status) {
        validateStatus(status);
        
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));

        debt.setStatus(status);
        
        if ("PAID".equals(status)) {
            debt.setPaidAt(LocalDate.now());
        }

        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        return customerDebtMapper.toResponse(savedDebt);
    }

    /**
     * حذف الدين
     */
    @Transactional
    public void deleteDebt(Long debtId) {
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));
        
        // منع حذف الديون المدفوعة
        if ("PAID".equals(debt.getStatus())) {
            throw new ConflictException("Cannot delete paid debt");
        }
        
        customerDebtRepository.delete(debt);
    }

    /**
     * الحصول على إحصائيات الديون
     */
    public DebtStatistics getDebtStatistics() {
        List<CustomerDebt> allDebts = customerDebtRepository.findAll();
        
        long totalDebts = allDebts.size();
        long activeDebts = allDebts.stream().filter(d -> "ACTIVE".equals(d.getStatus())).count();
        long paidDebts = allDebts.stream().filter(d -> "PAID".equals(d.getStatus())).count();
        long overdueDebts = allDebts.stream().filter(d -> "OVERDUE".equals(d.getStatus())).count();
        
        Float totalAmount = allDebts.stream().map(CustomerDebt::getAmount).reduce(0f, Float::sum);
        Float totalPaid = allDebts.stream().map(CustomerDebt::getPaidAmount).reduce(0f, Float::sum);
        Float totalRemaining = allDebts.stream().map(CustomerDebt::getRemainingAmount).reduce(0f, Float::sum);
        
        return DebtStatistics.builder()
                .totalDebts(totalDebts)
                .activeDebts(activeDebts)
                .paidDebts(paidDebts)
                .overdueDebts(overdueDebts)
                .totalAmount(totalAmount)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .build();
    }

    // Validation methods
    private void validateDebtRequest(CustomerDebtDTORequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debt request cannot be null");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Debt amount must be greater than zero");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
    }

    private void validatePaymentRequest(PayDebtDTORequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        if (request.getDebtId() == null) {
            throw new IllegalArgumentException("Debt ID is required");
        }
        if (request.getPaymentAmount() == null || request.getPaymentAmount().floatValue() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
    }

    private void validateCustomerExists(Long customerId) {
        if (!customerRepo.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }
    }

    private void validateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        if (!List.of("ACTIVE", "PAID", "OVERDUE").contains(status)) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE, PAID, or OVERDUE");
        }
    }

    // Inner class for statistics
    @lombok.Data
    @lombok.Builder
    public static class DebtStatistics {
        private long totalDebts;
        private long activeDebts;
        private long paidDebts;
        private long overdueDebts;
        private Float totalAmount;
        private Float totalPaid;
        private Float totalRemaining;
    }
} 