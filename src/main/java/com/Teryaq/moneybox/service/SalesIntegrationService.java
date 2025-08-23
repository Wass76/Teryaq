package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.Enum.TransactionType;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service to integrate sales operations with money box
 * This service automatically tracks cash sales in the money box
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesIntegrationService {

    private final MoneyBoxService moneyBoxService;

    /**
     * When a cash sale is created, automatically add to money box
     * This method should be called from the sales service
     */
    public MoneyBoxTransaction recordCashSale(Long pharmacyId, BigDecimal amount,
                                             String currency, Long saleInvoiceId, String invoiceNumber) {
        try {
            // Get current money box for pharmacy
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();

            if (currentBox.isPresent()) {
                log.info("Recording cash sale: {} {} for invoice {}", amount, currency, invoiceNumber);

                return moneyBoxService.addSaleTransaction(
                    currentBox.get().getId(),
                    amount,
                    currency,
                    saleInvoiceId,
                    invoiceNumber
                );
            } else {
                log.warn("No open money box found for pharmacy: {}. Cash sale not recorded.", pharmacyId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error recording cash sale: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * When a cash refund is processed, automatically subtract from money box
     */
    public MoneyBoxTransaction recordCashRefund(Long pharmacyId, BigDecimal amount,
                                               String currency, Long returnId, String returnNumber) {
        try {
            // Get current money box for pharmacy
            Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();

            if (currentBox.isPresent()) {
                log.info("Recording cash refund: {} {} for return {}", amount, currency, returnNumber);

                return moneyBoxService.addRefundTransaction(
                    currentBox.get().getId(),
                    amount,
                    currency,
                    returnId,
                    returnNumber
                );
            } else {
                log.warn("No open money box found for pharmacy: {}. Cash refund not recorded.", pharmacyId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error recording cash refund: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if pharmacy has open money box
     */
    public boolean hasOpenMoneyBox(Long pharmacyId) {
        return moneyBoxService.getCurrentMoneyBox().isPresent();
    }

    /**
     * Get current money box balance for pharmacy
     */
    public BigDecimal getCurrentBalance(Long pharmacyId) {
        Optional<MoneyBox> currentBox = moneyBoxService.getCurrentMoneyBox();
        if (currentBox.isPresent()) {
            return currentBox.get().getOpeningBalance()
                .add(currentBox.get().getTotalCashIn())
                .subtract(currentBox.get().getTotalCashOut());
        }
        return BigDecimal.ZERO;
    }
}
