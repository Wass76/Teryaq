package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.utils.exception.ConflictException;
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
    private final ExchangeRateService exchangeRateService;
    
    /**
     * Records a purchase payment in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchasePayment(Long pharmacyId, Long purchaseId, BigDecimal amount, Currency currency) {
        log.info("Recording purchase payment: pharmacy={}, purchase={}, amount={}, currency={}", 
                pharmacyId, purchaseId, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Convert amount to SYP if it's not already in SYP
            BigDecimal amountInSYP = amount;
            BigDecimal exchangeRate = BigDecimal.ONE;
            Currency originalCurrency = currency;
            BigDecimal originalAmount = amount;
            
            if (!Currency.SYP.equals(currency)) {
                try {
                    amountInSYP = exchangeRateService.convertToSYP(amount, currency);
                    exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.SYP);
                    log.info("Converted {} {} to {} SYP using rate: {}", 
                            amount, currency, amountInSYP, exchangeRate);
                } catch (Exception e) {
                    log.warn("Failed to convert currency for purchase {}: {}. Using original amount.", 
                            purchaseId, e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.PURCHASE_PAYMENT);
            transaction.setAmount(amountInSYP.negate()); // Negative amount as it's an expense
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(Currency.SYP);
            transaction.setConvertedAmount(amountInSYP);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amountInSYP));
            transaction.setDescription("Purchase payment for purchase ID: " + purchaseId + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceId(String.valueOf(purchaseId));
            transaction.setReferenceType("PURCHASE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP)
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Purchase payment recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record purchase payment for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase payment in MoneyBox", e);
        }
    }
    
    /**
     * Records a purchase refund in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchaseRefund(Long pharmacyId, Long purchaseId, BigDecimal amount, Currency currency) {
        log.info("Recording purchase refund: pharmacy={}, purchase={}, amount={}, currency={}", 
                pharmacyId, purchaseId, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Convert amount to SYP if it's not already in SYP
            BigDecimal amountInSYP = amount;
            BigDecimal exchangeRate = BigDecimal.ONE;
            Currency originalCurrency = currency;
            BigDecimal originalAmount = amount;
            
            if (!Currency.SYP.equals(currency)) {
                try {
                    amountInSYP = exchangeRateService.convertToSYP(amount, currency);
                    exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.SYP);
                    log.info("Converted {} {} to {} SYP using rate: {}", 
                            amount, currency, amountInSYP, exchangeRate);
                } catch (Exception e) {
                    log.warn("Failed to convert currency for purchase refund {}: {}. Using original amount.", 
                            purchaseId, e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.INCOME); // Treat refund as income
            transaction.setAmount(amountInSYP);
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(Currency.SYP);
            transaction.setConvertedAmount(amountInSYP);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().add(amountInSYP));
            transaction.setDescription("Refund for purchase ID: " + purchaseId + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceId(String.valueOf(purchaseId));
            transaction.setReferenceType("PURCHASE_REFUND");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP)
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Purchase refund recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record purchase refund for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase refund in MoneyBox", e);
        }
    }
    
    /**
     * Gets total purchase amount for a period in SYP (converted from all currencies)
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
     * Records an expense payment with automatic currency conversion to SYP
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordExpense(Long pharmacyId, String expenseDescription, BigDecimal amount, Currency currency) {
        log.info("Recording expense: pharmacy={}, description={}, amount={}, currency={}", 
                pharmacyId, expenseDescription, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Convert amount to SYP if it's not already in SYP
            BigDecimal amountInSYP = amount;
            BigDecimal exchangeRate = BigDecimal.ONE;
            Currency originalCurrency = currency;
            BigDecimal originalAmount = amount;
            
            if (!Currency.SYP.equals(currency)) {
                try {
                    amountInSYP = exchangeRateService.convertToSYP(amount, currency);
                    exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.SYP);
                    log.info("Converted {} {} to {} SYP using rate: {}", 
                            amount, currency, amountInSYP, exchangeRate);
                } catch (Exception e) {
                    log.warn("Failed to convert currency for expense: {}. Using original amount.", e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.EXPENSE);
            transaction.setAmount(amountInSYP.negate()); // Negative amount as it's an expense
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(Currency.SYP);
            transaction.setConvertedAmount(amountInSYP);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amountInSYP));
            transaction.setDescription(expenseDescription + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceType("EXPENSE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP)
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Expense recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record expense: pharmacy={}, description={}, amount={}", 
                     pharmacyId, expenseDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record expense in MoneyBox", e);
        }
    }
    
    /**
     * Records income with automatic currency conversion to SYP
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordIncome(Long pharmacyId, String incomeDescription, BigDecimal amount, Currency currency) {
        log.info("Recording income: pharmacy={}, description={}, amount={}, currency={}", 
                pharmacyId, incomeDescription, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Convert amount to SYP if it's not already in SYP
            BigDecimal amountInSYP = amount;
            BigDecimal exchangeRate = BigDecimal.ONE;
            Currency originalCurrency = currency;
            BigDecimal originalAmount = amount;
            
            if (!Currency.SYP.equals(currency)) {
                try {
                    amountInSYP = exchangeRateService.convertToSYP(amount, currency);
                    exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.SYP);
                    log.info("Converted {} {} to {} SYP using rate: {}", 
                            amount, currency, amountInSYP, exchangeRate);
                } catch (Exception e) {
                    log.warn("Failed to convert currency for income: {}. Using original amount.", e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.INCOME);
            transaction.setAmount(amountInSYP);
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(Currency.SYP);
            transaction.setConvertedAmount(amountInSYP);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().add(amountInSYP));
            transaction.setDescription(incomeDescription + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceType("INCOME");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP)
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Income recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record income: pharmacy={}, description={}, amount={}", 
                     pharmacyId, incomeDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record income in MoneyBox", e);
        }
    }
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new ConflictException("Money box not found for pharmacy: " + pharmacyId));
    }
}
