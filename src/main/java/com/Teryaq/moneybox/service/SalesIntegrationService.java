package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.moneybox.service.EnhancedMoneyBoxAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesIntegrationService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final EnhancedMoneyBoxAuditService enhancedAuditService;
    
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
            
            // Record transaction using enhanced audit service (it will handle balance updates)
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.SALE_PAYMENT,
                originalAmount,
                originalCurrency,
                "Sale payment for sale ID: " + saleId + 
                (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""),
                String.valueOf(saleId),
                "SALE",
                null, // userId - would need to be passed from calling service
                null, // userType - would need to be passed from calling service
                null, // ipAddress - would need to be passed from calling service
                null, // userAgent - would need to be passed from calling service
                null, // sessionId - would need to be passed from calling service
                Map.of("saleId", saleId, "pharmacyId", pharmacyId, "conversionRate", exchangeRate)
            );
            
            log.info("Sale payment recorded successfully using enhanced audit service. Amount: {} {}", 
                    originalAmount, originalCurrency);
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
            
            // Record transaction using enhanced audit service (it will handle balance updates)
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.SALE_REFUND,
                originalAmount,
                originalCurrency,
                "Sale refund for sale ID: " + saleId + 
                (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""),
                String.valueOf(saleId),
                "SALE_REFUND",
                null, // userId - would need to be passed from calling service
                null, // userType - would need to be passed from calling service
                null, // ipAddress - would need to be passed from calling service
                null, // userAgent - would need to be passed from calling service
                null, // sessionId - would need to be passed from calling service
                Map.of("saleId", saleId, "pharmacyId", pharmacyId, "conversionRate", exchangeRate)
            );
            
            log.info("Sale refund recorded successfully using enhanced audit service. Amount: {} {}", 
                    originalAmount, originalCurrency);
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
