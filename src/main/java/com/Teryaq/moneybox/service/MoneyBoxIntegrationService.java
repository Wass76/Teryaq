package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.Enum.TransactionType;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service to integrate Money Box with sales and purchase operations
 * This service automatically records cash transactions in the Money Box
 */
@Service
@Slf4j
public class MoneyBoxIntegrationService extends BaseSecurityService {
    
    private final MoneyBoxService moneyBoxService;
    
    public MoneyBoxIntegrationService(UserRepository userRepository, MoneyBoxService moneyBoxService) {
        super(userRepository);
        this.moneyBoxService = moneyBoxService;
    }
    
    /**
     * Record cash sale in Money Box (INCREASES money box)
     * Called when a cash sale is completed
     */
    public void recordCashSale(BigDecimal amount, String currency, Long saleInvoiceId, String invoiceNumber) {
        try {
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();
            
            if (currentBox.isPresent()) {
                log.info("Recording cash sale: {} {} for invoice {}", amount, currency, invoiceNumber);
                
                moneyBoxService.addSaleTransaction(
                    currentBox.get().getId(),
                    amount,
                    currency,
                    saleInvoiceId,
                    invoiceNumber
                );
                
                log.info("Cash sale recorded successfully in Money Box");
            } else {
                log.warn("No open Money Box found. Cash sale not recorded in Money Box.");
            }
        } catch (Exception e) {
            log.error("Error recording cash sale in Money Box: {}", e.getMessage(), e);
            // Don't throw exception - Money Box failure shouldn't prevent sale completion
        }
    }
    
    /**
     * Record cash purchase in Money Box (DECREASES money box)
     * Called when a cash purchase is completed
     */
    public void recordCashPurchase(BigDecimal amount, String currency, Long purchaseInvoiceId, String invoiceNumber) {
        try {
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();
            
            if (currentBox.isPresent()) {
                log.info("Recording cash purchase: {} {} for invoice {}", amount, currency, invoiceNumber);
                
                moneyBoxService.addPurchaseTransaction(
                    currentBox.get().getId(),
                    amount,
                    currency,
                    purchaseInvoiceId,
                    invoiceNumber
                );
                
                log.info("Cash purchase recorded successfully in Money Box");
            } else {
                log.warn("No open Money Box found. Cash purchase not recorded in Money Box.");
            }
        } catch (Exception e) {
            log.error("Error recording cash purchase in Money Box: {}", e.getMessage(), e);
            // Don't throw exception - Money Box failure shouldn't prevent purchase completion
        }
    }
    
    /**
     * Record cash refund in Money Box (DECREASES money box)
     * Called when a cash refund is processed
     */
    public void recordCashRefund(BigDecimal amount, String currency, Long returnId, String returnNumber) {
        try {
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();
            
            if (currentBox.isPresent()) {
                log.info("Recording cash refund: {} {} for return {}", amount, currency, returnNumber);
                
                moneyBoxService.addRefundTransaction(
                    currentBox.get().getId(),
                    amount,
                    currency,
                    returnId,
                    returnNumber
                );
                
                log.info("Cash refund recorded successfully in Money Box");
            } else {
                log.warn("No open Money Box found. Cash refund not recorded in Money Box.");
            }
        } catch (Exception e) {
            log.error("Error recording cash refund in Money Box: {}", e.getMessage(), e);
            // Don't throw exception - Money Box failure shouldn't prevent refund completion
        }
    }
    
    /**
     * Check if current user has access to Money Box
     */
    public boolean hasMoneyBoxAccess() {
        try {
            getCurrentUserPharmacyId();
            return true;
        } catch (UnAuthorizedException e) {
            return false;
        }
    }
    
    /**
     * Get current Money Box balance
     */
    public BigDecimal getCurrentBalance() {
        try {
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();
            if (currentBox.isPresent()) {
                MoneyBox box = currentBox.get();
                return box.getOpeningBalance()
                    .add(box.getTotalCashIn())
                    .subtract(box.getTotalCashOut());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error getting current Money Box balance: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
}
