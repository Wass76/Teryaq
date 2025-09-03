package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.user.Enum.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesIntegrationService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    
    /**
     * Records a sale payment in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the sales service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordSalePayment(Long pharmacyId, Long saleId, BigDecimal amount, Currency currency) {
        log.info("Recording sale payment: pharmacy={}, sale={}, amount={}, currency={}", 
                pharmacyId, saleId, amount, currency);
        
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
                    log.warn("Failed to convert currency for sale {}: {}. Using original amount.", 
                            saleId, e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.SALE_PAYMENT);
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
            transaction.setDescription("Sale payment for sale ID: " + saleId + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceId(String.valueOf(saleId));
            transaction.setReferenceType("SALE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP)
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Sale payment recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record sale payment for sale {}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Failed to record sale payment in MoneyBox", e);
        }
    }
    
    /**
     * Records a sale refund in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the sales service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordSaleRefund(Long pharmacyId, Long saleId, BigDecimal amount, Currency currency) {
        log.info("Recording sale refund: pharmacy={}, sale={}, amount={}, currency={}", 
                pharmacyId, saleId, amount, currency);
        
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
                    log.warn("Failed to convert currency for sale refund {}: {}. Using original amount.", 
                            saleId, e.getMessage());
                    // Fallback: use original amount but mark as unconverted
                    amountInSYP = amount;
                    exchangeRate = BigDecimal.ZERO;
                }
            }
            
            // Create transaction record with enhanced currency information
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.SALE_REFUND); // Use specific refund type
            transaction.setAmount(amountInSYP.negate()); // Negative amount for refund (money going out)
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(Currency.SYP);
            transaction.setConvertedAmount(amountInSYP);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amountInSYP));
            transaction.setDescription("Sale refund for sale ID: " + saleId + 
                                   (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
            transaction.setReferenceId(String.valueOf(saleId));
            transaction.setReferenceType("SALE_REFUND");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance (always in SYP) - subtract because we're giving money back to customer
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amountInSYP));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Sale refund recorded successfully. Amount: {} {} -> {} SYP. New balance: {}", 
                    originalAmount, originalCurrency, amountInSYP, moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record sale refund for sale {}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Failed to record sale refund in MoneyBox", e);
        }
    }
    
    /**
     * Gets total sales amount for a period in SYP (converted from all currencies)
     */
    public BigDecimal getSalesAmountForPeriod(Long pharmacyId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            BigDecimal totalSales = transactionRepository.getTotalAmountByTypeAndPeriod(
                    moneyBox.getId(), TransactionType.SALE_PAYMENT, startDate, endDate);
            
            return totalSales != null ? totalSales : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Failed to get sales amount for period: pharmacy={}, start={}, end={}", 
                     pharmacyId, startDate, endDate, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Money box not found for pharmacy: " + pharmacyId));
    }
}
