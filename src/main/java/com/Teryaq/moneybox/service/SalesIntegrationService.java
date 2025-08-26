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
public class SalesIntegrationService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    
    /**
     * Records a sale payment in the money box
     * This method is designed to be called within a transaction from the sales service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordSalePayment(Long pharmacyId, Long saleId, BigDecimal amount, String currency) {
        log.info("Recording sale payment: pharmacy={}, sale={}, amount={}", pharmacyId, saleId, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.SALE_PAYMENT);
            transaction.setAmount(amount);
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().add(amount));
            transaction.setDescription("Sale payment for sale ID: " + saleId);
            transaction.setReferenceId(String.valueOf(saleId));
            transaction.setReferenceType("SALE");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().add(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Sale payment recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record sale payment for sale {}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Failed to record sale payment in MoneyBox", e);
        }
    }
    
    /**
     * Records a sale refund in the money box
     * This method is designed to be called within a transaction from the sales service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordSaleRefund(Long pharmacyId, Long saleId, BigDecimal amount, String currency) {
        log.info("Recording sale refund: pharmacy={}, sale={}, amount={}", pharmacyId, saleId, amount);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Create transaction record
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(TransactionType.CASH_WITHDRAWAL); // Treat refund as withdrawal
            transaction.setAmount(amount.negate()); // Negative amount for refund
            transaction.setCurrency(currency);
            transaction.setBalanceBefore(moneyBox.getCurrentBalance());
            transaction.setBalanceAfter(moneyBox.getCurrentBalance().subtract(amount));
            transaction.setDescription("Refund for sale ID: " + saleId);
            transaction.setReferenceId(String.valueOf(saleId));
            transaction.setReferenceType("SALE_REFUND");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // Update money box balance
            moneyBox.setCurrentBalance(moneyBox.getCurrentBalance().subtract(amount));
            
            // Save both transaction and updated money box
            transactionRepository.save(transaction);
            moneyBoxRepository.save(moneyBox);
            
            log.info("Sale refund recorded successfully. New balance: {}", moneyBox.getCurrentBalance());
        } catch (Exception e) {
            log.error("Failed to record sale refund for sale {}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Failed to record sale refund in MoneyBox", e);
        }
    }
    
    /**
     * Gets total sales amount for a period
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
