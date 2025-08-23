package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import com.Teryaq.moneybox.Enum.PeriodType;
import com.Teryaq.moneybox.Enum.TransactionType;
import com.Teryaq.moneybox.dto.MoneyBoxSummary;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.moneybox.service.ExchangeRateService;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.utils.exception.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MoneyBoxService extends BaseSecurityService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    
    public MoneyBoxService(UserRepository userRepository, 
                          MoneyBoxRepository moneyBoxRepository,
                          MoneyBoxTransactionRepository transactionRepository,
                          ExchangeRateService exchangeRateService) {
        super(userRepository);
        this.moneyBoxRepository = moneyBoxRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
    }
    
    /**
     * Open a new money box for the day
     */
    public MoneyBox openMoneyBox(BigDecimal openingBalance, String notes) {
        try {
            Long pharmacyId = getCurrentUserPharmacyId();
            
            // Check if money box already exists for today
            LocalDate today = LocalDate.now();
            if (moneyBoxRepository.existsByPharmacyIdAndBusinessDateAndStatus(pharmacyId, today, MoneyBoxStatus.OPEN)) {
                throw new IllegalStateException("Money box already open for today");
            }
            
            MoneyBox moneyBox = MoneyBox.builder()
                .pharmacyId(pharmacyId)
                .businessDate(today)
                .status(MoneyBoxStatus.OPEN)
                .periodType(PeriodType.DAILY)
                .openingBalance(openingBalance)
                .openedAt(LocalDateTime.now())
                .openedBy(getCurrentUser().getId())
                .openingNotes(notes)
                .totalCashIn(BigDecimal.ZERO)
                .totalCashOut(BigDecimal.ZERO)
                .netCashFlow(BigDecimal.ZERO)
                .build();
            
            return moneyBoxRepository.save(moneyBox);
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("User is not associated with any pharmacy. Cannot open money box.");
        }
    }
    
    /**
     * Close the money box for the day
     */
    public MoneyBox closeMoneyBox(Long moneyBoxId, BigDecimal actualBalance, String notes) {
        try {
            MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                .orElseThrow(() -> new EntityNotFoundException("Money box not found"));
            
            // Validate pharmacy access
            validatePharmacyAccess(moneyBox.getPharmacyId());
            
            if (moneyBox.getStatus() != MoneyBoxStatus.OPEN) {
                throw new IllegalStateException("Money box is not open");
            }
            
            // Calculate expected balance
            BigDecimal expectedBalance = moneyBox.getOpeningBalance()
                .add(moneyBox.getTotalCashIn())
                .subtract(moneyBox.getTotalCashOut());
            
            moneyBox.setExpectedBalance(expectedBalance);
            moneyBox.setActualBalance(actualBalance);
            moneyBox.setClosingBalance(actualBalance);
            moneyBox.setClosedAt(LocalDateTime.now());
            moneyBox.setClosedBy(getCurrentUser().getId());
            moneyBox.setClosingNotes(notes);
            moneyBox.setStatus(MoneyBoxStatus.RECONCILED);
            
            return moneyBoxRepository.save(moneyBox);
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("User is not associated with any pharmacy. Cannot close money box.");
        }
    }
    
    /**
     * Add a transaction to the money box
     */
    public MoneyBoxTransaction addTransaction(Long moneyBoxId, TransactionType type, 
                                           BigDecimal amount, String currency, String description) {
        try {
            MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                .orElseThrow(() -> new EntityNotFoundException("Money box not found"));
            
            // Validate pharmacy access
            validatePharmacyAccess(moneyBox.getPharmacyId());
            
            if (moneyBox.getStatus() != MoneyBoxStatus.OPEN) {
                throw new IllegalStateException("Money box is not open");
            }
            
            // Convert amount to SYP for consistency
            BigDecimal amountInSYP = exchangeRateService.convertToSYP(amount, currency);
            BigDecimal exchangeRate = exchangeRateService.getExchangeRate(currency, "SYP");
            
            MoneyBoxTransaction transaction = MoneyBoxTransaction.builder()
                .moneyBoxId(moneyBoxId)
                .transactionDate(LocalDateTime.now())
                .transactionType(type)
                .amount(amount)
                .currency(currency)
                .exchangeRate(exchangeRate)
                .amountInSYP(amountInSYP)
                .description(description)
                .status("COMPLETED")
                .employeeId(getCurrentUser().getId())
                .requiresApproval(false) // Set explicitly to avoid null constraint violation
                .build();
            
            transaction = transactionRepository.save(transaction);
            
            // Update money box totals
            updateMoneyBoxTotals(moneyBoxId);
            
            return transaction;
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("User is not associated with any pharmacy. Cannot add transaction.");
        }
    }
    
    /**
     * Add sale transaction (cash received) - INCREASES money box
     */
    public MoneyBoxTransaction addSaleTransaction(Long moneyBoxId, BigDecimal amount, 
                                                String currency, Long saleInvoiceId, String invoiceNumber) {
        MoneyBoxTransaction transaction = addTransaction(moneyBoxId, TransactionType.SALE, 
                                                       amount, currency, "Cash sale");
        
        // Set reference information
        transaction.setReferenceId(saleInvoiceId);
        transaction.setReferenceType("SALE_INVOICE");
        transaction.setReferenceNumber(invoiceNumber);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Add purchase transaction (cash paid to supplier) - DECREASES money box
     */
    public MoneyBoxTransaction addPurchaseTransaction(Long moneyBoxId, BigDecimal amount, 
                                                    String currency, Long purchaseInvoiceId, String invoiceNumber) {
        MoneyBoxTransaction transaction = addTransaction(moneyBoxId, TransactionType.PURCHASE, 
                                                       amount, currency, "Cash purchase");
        
        // Set reference information
        transaction.setReferenceId(purchaseInvoiceId);
        transaction.setReferenceType("PURCHASE_INVOICE");
        transaction.setReferenceNumber(invoiceNumber);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Add refund transaction (cash returned to customer) - DECREASES money box
     */
    public MoneyBoxTransaction addRefundTransaction(Long moneyBoxId, BigDecimal amount, 
                                                   String currency, Long returnId, String returnNumber) {
        MoneyBoxTransaction transaction = addTransaction(moneyBoxId, TransactionType.REFUND, 
                                                       amount, currency, "Customer refund");
        
        // Set reference information
        transaction.setReferenceId(returnId);
        transaction.setReferenceType("CUSTOMER_RETURN");
        transaction.setReferenceNumber(returnNumber);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Add withdrawal transaction (cash taken from box) - DECREASES money box
     */
    public MoneyBoxTransaction addWithdrawalTransaction(Long moneyBoxId, BigDecimal amount, 
                                                      String currency, String reason, String receiptNumber) {
        MoneyBoxTransaction transaction = addTransaction(moneyBoxId, TransactionType.WITHDRAWAL, 
                                                       amount, currency, "Cash withdrawal: " + reason);
        
        transaction.setReceiptNumber(receiptNumber);
        transaction.setNotes(reason);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Add deposit transaction (cash added to box) - INCREASES money box
     */
    public MoneyBoxTransaction addDepositTransaction(Long moneyBoxId, BigDecimal amount, 
                                                    String currency, String reason, String receiptNumber) {
        MoneyBoxTransaction transaction = addTransaction(moneyBoxId, TransactionType.DEPOSIT, 
                                                       amount, currency, "Cash deposit: " + reason);
        
        transaction.setReceiptNumber(receiptNumber);
        transaction.setNotes(reason);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Get current money box for current user's pharmacy
     */
    public Optional<MoneyBox> getCurrentMoneyBox() {
        try {
            Long pharmacyId = getCurrentUserPharmacyId();
            LocalDate today = LocalDate.now();
            return moneyBoxRepository.findByPharmacyIdAndBusinessDateAndStatus(
                pharmacyId, today, MoneyBoxStatus.OPEN);
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("User is not associated with any pharmacy. Cannot access money box.");
        }
    }
    
    /**
     * Get money box summary
     */
    public MoneyBoxSummary getMoneyBoxSummary(Long moneyBoxId) {
        try {
            MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                .orElseThrow(() -> new EntityNotFoundException("Money box not found"));
            
            // Validate pharmacy access
            validatePharmacyAccess(moneyBox.getPharmacyId());
            
            List<MoneyBoxTransaction> transactions = transactionRepository.findTransactionsByMoneyBox(moneyBoxId);
            
            return MoneyBoxSummary.builder()
                .moneyBoxId(moneyBoxId)
                .openingBalance(moneyBox.getOpeningBalance())
                .currentBalance(calculateCurrentBalance(moneyBox))
                .totalCashIn(moneyBox.getTotalCashIn())
                .totalCashOut(moneyBox.getTotalCashOut())
                .netCashFlow(moneyBox.getNetCashFlow())
                .transactionCount(transactions.size())
                .lastTransactionDate(transactions.isEmpty() ? null : transactions.get(0).getTransactionDate())
                .build();
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException("User is not associated with any pharmacy. Cannot access money box summary.");
        }
    }
    
    /**
     * Calculate current balance based on opening balance and transactions
     */
    private BigDecimal calculateCurrentBalance(MoneyBox moneyBox) {
        return moneyBox.getOpeningBalance()
            .add(moneyBox.getTotalCashIn())
            .subtract(moneyBox.getTotalCashOut());
    }
    
    /**
     * Update money box totals based on transactions
     */
    private void updateMoneyBoxTotals(Long moneyBoxId) {
        MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
            .orElseThrow(() -> new EntityNotFoundException("Money box not found"));
        
        // Get totals for each transaction type, handling null values safely
        BigDecimal totalCashIn = BigDecimal.ZERO;
        BigDecimal totalCashOut = BigDecimal.ZERO;
        
        // Cash In transactions (INCREASES money box)
        BigDecimal saleTotal = transactionRepository.sumAmountByType(moneyBoxId, TransactionType.SALE);
        BigDecimal depositTotal = transactionRepository.sumAmountByType(moneyBoxId, TransactionType.DEPOSIT);
        if (saleTotal != null) totalCashIn = totalCashIn.add(saleTotal);
        if (depositTotal != null) totalCashIn = totalCashIn.add(depositTotal);
        
        // Cash Out transactions (DECREASES money box)
        BigDecimal purchaseTotal = transactionRepository.sumAmountByType(moneyBoxId, TransactionType.PURCHASE);
        BigDecimal refundTotal = transactionRepository.sumAmountByType(moneyBoxId, TransactionType.REFUND);
        BigDecimal withdrawalTotal = transactionRepository.sumAmountByType(moneyBoxId, TransactionType.WITHDRAWAL);
        
        if (purchaseTotal != null) totalCashOut = totalCashOut.add(purchaseTotal);
        if (refundTotal != null) totalCashOut = totalCashOut.add(refundTotal);
        if (withdrawalTotal != null) totalCashOut = totalCashOut.add(withdrawalTotal);
        
        BigDecimal netCashFlow = totalCashIn.subtract(totalCashOut);
        
        moneyBox.setTotalCashIn(totalCashIn);
        moneyBox.setTotalCashOut(totalCashOut);
        moneyBox.setNetCashFlow(netCashFlow);
        
        moneyBoxRepository.save(moneyBox);
    }
}
