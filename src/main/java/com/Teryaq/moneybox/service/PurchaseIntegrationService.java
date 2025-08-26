package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseIntegrationService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    
    /**
     * Records a purchase payment in the money box
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchasePayment(Long pharmacyId, Long purchaseId, BigDecimal amount, String currency) {
        log.info("Recording purchase payment: pharmacy={}, purchase={}, amount={}", pharmacyId, purchaseId, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.PURCHASE_PAYMENT);
            transaction.setAmount(amount.negate()); // Negative amount as it's an expense
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amount));
            transaction.setDescription("Purchase payment for purchase ID: " + purchaseId);
            transaction.setReferenceId(String.valueOf(purchaseId));
            transaction.setReferenceType("PURCHASE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Purchase payment recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record purchase payment for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase payment in MoneyBox", e);
        }
    }
    
    /**
     * Records a purchase refund/return in the money box
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchaseRefund(Long pharmacyId, Long purchaseId, BigDecimal amount, String currency) {
        log.info("Recording purchase refund: pharmacy={}, purchase={}, amount={}", pharmacyId, purchaseId, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.INCOME); // Treat refund as income
            transaction.setAmount(amount);
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().add(amount));
            transaction.setDescription("Refund for purchase ID: " + purchaseId);
            transaction.setReferenceId(String.valueOf(purchaseId));
            transaction.setReferenceType("PURCHASE_REFUND");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Purchase refund recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record purchase refund for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase refund in MoneyBox", e);
        }
    }
    
    /**
     * Gets total purchase amount for a period
     */
    public BigDecimal getPurchaseAmountForPeriod(Long pharmacyId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            BigDecimal totalPurchases = transactionRepository.getTotalAmountByTypeAndPeriod(
                    moneyBox.getId(), TransactionType.PURCHASE_PAYMENT, startDate, endDate);
            
            return totalPurchases != null ? totalPurchases.abs() : BigDecimal.ZERO; // Return absolute value
        } catch (Exception e) {
            log.error("Failed to get purchase amount for period: pharmacy={}, start={}, end={}", 
                     pharmacyId, startDate, endDate, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Records an expense payment
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordExpense(Long pharmacyId, String expenseDescription, BigDecimal amount, String currency) {
        log.info("Recording expense: pharmacy={}, description={}, amount={}", pharmacyId, expenseDescription, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.EXPENSE);
            transaction.setAmount(amount.negate()); // Negative amount as it's an expense
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amount));
            transaction.setDescription(expenseDescription);
            transaction.setReferenceType("EXPENSE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Expense recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record expense: pharmacy={}, description={}, amount={}", 
                     pharmacyId, expenseDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record expense in MoneyBox", e);
        }
    }
    
    /**
     * Records income
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordIncome(Long pharmacyId, String incomeDescription, BigDecimal amount, String currency) {
        log.info("Recording income: pharmacy={}, description={}, amount={}", pharmacyId, incomeDescription, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.INCOME);
            transaction.setAmount(amount);
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().add(amount));
            transaction.setDescription(incomeDescription);
            transaction.setReferenceType("INCOME");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Income recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record income: pharmacy={}, description={}, amount={}", 
                     pharmacyId, incomeDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record income in MoneyBox", e);
        }
    }
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Money box not found for pharmacy: " + pharmacyId));
    }
}
