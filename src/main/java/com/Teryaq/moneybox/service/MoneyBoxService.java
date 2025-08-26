package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.MoneyBoxStatus;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.mapper.MoneyBoxMapper;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.user.service.BaseSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MoneyBoxService extends BaseSecurityService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    
    public MoneyBoxService(MoneyBoxRepository moneyBoxRepository,
                          MoneyBoxTransactionRepository transactionRepository,
                          com.Teryaq.user.repository.UserRepository userRepository) {
        super(userRepository);
        this.moneyBoxRepository = moneyBoxRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Transactional
    public MoneyBoxResponseDTO createMoneyBox(MoneyBoxRequestDTO request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Creating new money box for pharmacy: {}", currentPharmacyId);
        
        // Check if pharmacy already has a money box
        if (moneyBoxRepository.findByPharmacyId(currentPharmacyId).isPresent()) {
            throw new IllegalStateException("Pharmacy already has a money box");
        }
        
        MoneyBox moneyBox = MoneyBoxMapper.toEntity(request);
        moneyBox.setPharmacyId(currentPharmacyId); // Set pharmacyId from current user context
        moneyBox.setStatus(MoneyBoxStatus.OPEN); // Set to OPEN status
        
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        
        // Create opening balance transaction record
        createTransactionRecord(savedMoneyBox, TransactionType.OPENING_BALANCE, 
                              request.getInitialBalance(), request.getInitialBalance(), 
                              "Initial money box balance", null, null, request.getCurrency());
        
        log.info("Money box created for pharmacy: {}", savedMoneyBox.getPharmacyId());
        
        return MoneyBoxMapper.toResponseDTO(savedMoneyBox);
    }
    
    public MoneyBoxResponseDTO getMoneyBoxByCurrentPharmacy() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        return MoneyBoxMapper.toResponseDTO(moneyBox);
    }
    
    @Transactional
    public MoneyBoxResponseDTO addTransaction(BigDecimal amount, String description) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Adding manual transaction for pharmacy: {}, amount: {}, description: {}", 
                currentPharmacyId, amount, description);
        
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        if (moneyBox.getStatus() != MoneyBoxStatus.OPEN) {
            throw new IllegalStateException("Money box is not open");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Transaction amount cannot be zero");
        }
        
        BigDecimal balanceBefore = moneyBox.getCurrentBalance();
        BigDecimal newBalance = balanceBefore.add(amount);
        
        // Use more descriptive transaction types for manual transactions
        TransactionType transactionType = (amount.compareTo(BigDecimal.ZERO) > 0) 
            ? TransactionType.CASH_DEPOSIT 
            : TransactionType.CASH_WITHDRAWAL;
        
        // Update money box balance
        moneyBox.setCurrentBalance(newBalance);
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        
        // Create transaction record with descriptive description
        String transactionDescription = (amount.compareTo(BigDecimal.ZERO) > 0) 
            ? "Manual cash deposit: " + (description != null ? description : "Cash added to money box")
            : "Manual cash withdrawal: " + (description != null ? description : "Cash removed from money box");
        
        createTransactionRecord(savedMoneyBox, transactionType, amount, balanceBefore, 
                              transactionDescription, null, null, moneyBox.getCurrency());
        
        log.info("Manual {} transaction added. New balance for pharmacy {}: {}", 
                transactionType, currentPharmacyId, newBalance);
        
        return MoneyBoxMapper.toResponseDTO(savedMoneyBox);
    }
    
    @Transactional
    public MoneyBoxResponseDTO reconcileCash(BigDecimal actualCashCount, String notes) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Reconciling cash for pharmacy: {}, actual count: {}", currentPharmacyId, actualCashCount);
        
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        BigDecimal balanceBefore = moneyBox.getCurrentBalance();
        BigDecimal difference = actualCashCount.subtract(balanceBefore);
        
        // Update money box
        moneyBox.setReconciledBalance(actualCashCount);
        moneyBox.setLastReconciled(LocalDateTime.now());
        
        // If there's a difference, create adjustment transaction
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            moneyBox.setCurrentBalance(actualCashCount);
            
            // Create adjustment transaction record
            createTransactionRecord(moneyBox, TransactionType.ADJUSTMENT, difference, balanceBefore, 
                                  notes != null ? notes : "Cash reconciliation adjustment", 
                                  null, null, moneyBox.getCurrency());
        }
        
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        log.info("Cash reconciled for pharmacy: {}", currentPharmacyId);
        
        return MoneyBoxMapper.toResponseDTO(savedMoneyBox);
    }
    
    public MoneyBoxSummary getPeriodSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        // Get income transactions (positive amounts)
        BigDecimal totalIncome = transactionRepository.getTotalAmountByTypeAndPeriod(
            moneyBox.getId(), TransactionType.INCOME, startDate, endDate);
        
        // Get expense transactions (negative amounts)
        BigDecimal totalExpense = transactionRepository.getTotalAmountByTypeAndPeriod(
            moneyBox.getId(), TransactionType.EXPENSE, startDate, endDate);
        
        BigDecimal netAmount = totalIncome.add(totalExpense); // totalExpense is already negative
        
        return new MoneyBoxSummary(totalIncome, totalExpense.abs(), netAmount, startDate, endDate);
    }
    
    /**
     * Helper method to create transaction records
     */
    private void createTransactionRecord(MoneyBox moneyBox, TransactionType type, BigDecimal amount, 
                                       BigDecimal balanceBefore, String description, 
                                       String referenceId, String referenceType, String currency) {
        MoneyBoxTransaction transaction = new MoneyBoxTransaction();
        transaction.setMoneyBox(moneyBox);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.add(amount));
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transaction.setCurrency(currency != null ? currency : "SYP");
        transaction.setCreatedBy(getCurrentUser().getUsername());
        
        transactionRepository.save(transaction);
        log.debug("Created transaction record: type={}, amount={}, description={}", 
                type, amount, description);
    }
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Money box not found for pharmacy: " + pharmacyId));
    }
    
    // Inner class for summary
    public static class MoneyBoxSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netAmount;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        
        // Constructor, getters, setters...
        public MoneyBoxSummary() {}
        
        public MoneyBoxSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netAmount, 
                             LocalDateTime periodStart, LocalDateTime periodEnd) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netAmount = netAmount;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }
        
        // Getters and setters
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
        
        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
        
        public LocalDateTime getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
        
        public LocalDateTime getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    }
}
