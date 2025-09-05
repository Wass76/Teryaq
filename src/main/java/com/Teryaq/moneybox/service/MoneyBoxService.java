package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.dto.CurrencyConversionResponseDTO;
import com.Teryaq.moneybox.dto.ExchangeRateResponseDTO;
import com.Teryaq.moneybox.dto.MoneyBoxRequestDTO;
import com.Teryaq.moneybox.dto.MoneyBoxResponseDTO;
import com.Teryaq.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.MoneyBoxStatus;
import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.mapper.MoneyBoxMapper;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.moneybox.repository.MoneyBoxTransactionRepository;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.Teryaq.moneybox.mapper.ExchangeRateMapper;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.product.dto.PaginationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@Slf4j
public class MoneyBoxService extends BaseSecurityService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserRepository userRepository;
    private final MoneyBoxMapper moneyBoxMapper;
    
    // Default exchange rates for production fallback
    private static final BigDecimal DEFAULT_USD_TO_SYP_RATE = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_EUR_TO_SYP_RATE = new BigDecimal("11000");
    
    public MoneyBoxService(MoneyBoxRepository moneyBoxRepository,
                          MoneyBoxTransactionRepository transactionRepository,
                          ExchangeRateService exchangeRateService,
                          com.Teryaq.user.repository.UserRepository userRepository,
                          MoneyBoxMapper moneyBoxMapper) {
        super(userRepository);
        this.moneyBoxRepository = moneyBoxRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
        this.userRepository = userRepository;
        this.moneyBoxMapper = moneyBoxMapper;
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
        
        // Always set currency to SYP for consistency
        moneyBox.setCurrency("SYP");
        
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        
        // Convert initial balance to SYP if it's not already in SYP
        BigDecimal initialBalanceInSYP = request.getInitialBalance();
        Currency requestCurrency = request.getCurrency();
        
        if (requestCurrency != null && !Currency.SYP.equals(requestCurrency)) {
            try {
                initialBalanceInSYP = exchangeRateService.convertToSYP(request.getInitialBalance(), requestCurrency);
                log.info("Converted initial balance from {} {} to {} SYP", 
                        request.getInitialBalance(), requestCurrency, initialBalanceInSYP);
            } catch (Exception e) {
                log.warn("Failed to convert initial balance currency: {}. Using original amount.", e.getMessage());
                // Fallback: use original amount
                initialBalanceInSYP = request.getInitialBalance();
            }
        }
        
        // Update the saved money box with converted balance
        savedMoneyBox.setCurrentBalance(initialBalanceInSYP);
        savedMoneyBox.setInitialBalance(initialBalanceInSYP);
        savedMoneyBox = moneyBoxRepository.save(savedMoneyBox);
        
        // Create opening balance transaction record
        createTransactionRecord(savedMoneyBox, TransactionType.OPENING_BALANCE, 
                              initialBalanceInSYP, initialBalanceInSYP, 
                              "Initial money box balance", null, null, 
                              requestCurrency != null ? requestCurrency : Currency.SYP);
        
        log.info("Money box created for pharmacy: {} with initial balance: {} SYP", 
                savedMoneyBox.getPharmacyId(), initialBalanceInSYP);
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(savedMoneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(savedMoneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(savedMoneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    public MoneyBoxResponseDTO getMoneyBoxByCurrentPharmacy() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(moneyBox);
        
        // Calculate currency conversions in service layer
        response.setTotalBalanceInUSD(calculateUSDBalance(moneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(moneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    /**
     * Set current exchange rates in MoneyBoxResponseDTO with fallback to defaults
     */
    private void setExchangeRates(MoneyBoxResponseDTO response) {
        // Get current exchange rates from database with fallback to defaults
        try {
            BigDecimal usdToSypRate = exchangeRateService.getExchangeRate(Currency.USD, Currency.SYP);
            response.setCurrentUSDToSYPRate(usdToSypRate);
        } catch (Exception e) {
            log.warn("Failed to get USD to SYP exchange rate from database, using default: {}", e.getMessage());
            // Use default rate from ExchangeRateService
            response.setCurrentUSDToSYPRate(ExchangeRateService.getDefaultUsdToSypRate());
        }
        
        try {
            BigDecimal eurToSypRate = exchangeRateService.getExchangeRate(Currency.EUR, Currency.SYP);
            response.setCurrentEURToSYPRate(eurToSypRate);
        } catch (Exception e) {
            log.warn("Failed to get EUR to SYP exchange rate from database, using default: {}", e.getMessage());
            // Use default rate from ExchangeRateService
            response.setCurrentEURToSYPRate(ExchangeRateService.getDefaultEurToSypRate());
        }
    }

    /**
     * Calculate USD balance from SYP balance with fallback to default rate
     */
    private BigDecimal calculateUSDBalance(BigDecimal balanceInSYP) {
        try {
            // Try to get current exchange rate from service
            BigDecimal rate = exchangeRateService.getExchangeRate(Currency.SYP, Currency.USD);
            return balanceInSYP.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            // Fallback to default rate if service fails
            BigDecimal defaultRate = BigDecimal.ONE.divide(DEFAULT_USD_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
            return balanceInSYP.multiply(defaultRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Calculate EUR balance from SYP balance with fallback to default rate
     */
    private BigDecimal calculateEURBalance(BigDecimal balanceInSYP) {
        try {
            // Try to get current exchange rate from service
            BigDecimal rate = exchangeRateService.getExchangeRate(Currency.SYP, Currency.EUR);
            return balanceInSYP.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            // Fallback to default rate if service fails
            BigDecimal defaultRate = BigDecimal.ONE.divide(DEFAULT_EUR_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
            return balanceInSYP.multiply(defaultRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    @Transactional
    public MoneyBoxResponseDTO addTransaction(BigDecimal amount, String description, Currency currency) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Adding manual transaction for pharmacy: {}, amount: {}, currency: {}, description: {}", 
                currentPharmacyId, amount, currency, description);
        
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        if (moneyBox.getStatus() != MoneyBoxStatus.OPEN) {
            throw new ConflictException("Money box is not open");
        }
        
        if (amount == null) {
            throw new ConflictException("Transaction amount cannot be null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException("Transaction amount cannot be zero");
        }
        
        // Convert amount to SYP if it's not already in SYP
        BigDecimal amountInSYP = amount;
        Currency originalCurrency = currency != null ? currency : Currency.SYP;
        
        if (!Currency.SYP.equals(originalCurrency)) {
            try {
                amountInSYP = exchangeRateService.convertToSYP(amount, originalCurrency);
                log.info("Converted {} {} to {} SYP", amount, originalCurrency, amountInSYP);
            } catch (Exception e) {
                log.warn("Failed to convert currency: {}. Using original amount.", e.getMessage());
                // Fallback: use original amount
                amountInSYP = amount;
            }
        }
        
        BigDecimal balanceBefore = moneyBox.getCurrentBalance();
        BigDecimal newBalance = balanceBefore.add(amountInSYP);
        
        // Use more descriptive transaction types for manual transactions
        TransactionType transactionType = (amountInSYP.compareTo(BigDecimal.ZERO) > 0) 
            ? TransactionType.CASH_DEPOSIT 
            : TransactionType.CASH_WITHDRAWAL;
        
        // Update money box balance
        moneyBox.setCurrentBalance(newBalance);
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        
        // Create transaction record with descriptive description
        String transactionDescription = (amountInSYP.compareTo(BigDecimal.ZERO) > 0) 
            ? "Manual cash deposit: " + (description != null ? description : "Cash added to money box")
            : "Manual cash withdrawal: " + (description != null ? description : "Cash removed from money box");
        
        createTransactionRecord(savedMoneyBox, transactionType, amountInSYP, balanceBefore, 
                              transactionDescription, null, null, originalCurrency);
        
        log.info("Manual {} transaction added. New balance for pharmacy {}: {}", 
                transactionType, currentPharmacyId, newBalance);
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(savedMoneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(savedMoneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(savedMoneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    @Transactional
    public MoneyBoxResponseDTO addTransaction(BigDecimal amount, String description) {
        return addTransaction(amount, description, Currency.SYP);
    }
    
    @Transactional
    public MoneyBoxResponseDTO reconcileCash(BigDecimal actualCashCount, String notes) {
        // Validate input parameters
        if (actualCashCount == null) {
            throw new ConflictException("Actual cash count cannot be null");
        }
        
        if (actualCashCount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException("Actual cash count cannot be negative");
        }
        
        if (notes == null) {
            throw new ConflictException("Reconciliation notes cannot be null");
        }
        
        if (notes.trim().isEmpty()) {
            throw new ConflictException("Reconciliation notes cannot be empty");
        }
        
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
                                  null, null, Currency.SYP);
        }
        
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        log.info("Cash reconciled for pharmacy: {}", currentPharmacyId);
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(savedMoneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(savedMoneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(savedMoneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
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
    
    public List<MoneyBoxTransactionResponseDTO> getAllTransactions(LocalDateTime startDate, LocalDateTime endDate, String transactionType) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        List<MoneyBoxTransaction> transactions;
        
        if (startDate != null && endDate != null) {

            if (transactionType != null) {
                TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
                transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    moneyBox.getId(), startDate, endDate);
                // Filter by transaction type
                transactions = transactions.stream()
                    .filter(t -> t.getTransactionType() == type)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    moneyBox.getId(), startDate, endDate);
            }
        } else if (transactionType != null) {
            // Filter only by transaction type
            TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
            transactions = transactionRepository.findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
                moneyBox.getId(), type);
        } else {
            // Get all transactions
            transactions = transactionRepository.findByMoneyBoxIdOrderByCreatedAtDesc(moneyBox.getId());
        }
        
        return transactions.stream()
            .map(transaction -> {
                // Get user email for each transaction
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    // Paginated methods
    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdOrderByCreatedAtDesc(moneyBox.getId(), pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            moneyBox.getId(), startDate, endDate, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            String transactionType, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
            moneyBox.getId(), type, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            LocalDateTime startDate, LocalDateTime endDate, String transactionType, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            moneyBox.getId(), type, startDate, endDate, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    /**
     * Helper method to get user email by user ID
     */
    private String getUserEmailById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to fetch user email for ID {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public CurrencyConversionResponseDTO convertCurrencyToSYP(BigDecimal amount, Currency fromCurrency) {
        try {
            BigDecimal convertedAmount = exchangeRateService.convertToSYP(amount, fromCurrency);
            BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromCurrency, Currency.SYP);
            
            return ExchangeRateMapper.toConversionResponse(
                amount, fromCurrency, convertedAmount, Currency.SYP, exchangeRate
            );
        } catch (Exception e) {
            log.warn("Failed to convert currency: {}. Using 1:1 rate.", e.getMessage());
            return ExchangeRateMapper.toConversionResponse(
                amount, fromCurrency, amount, Currency.SYP, BigDecimal.ONE, "FALLBACK"
            );
        }
    }

    public List<ExchangeRateResponseDTO> getCurrentExchangeRates() {
        return exchangeRateService.getAllActiveRates();
    }
    
    /**
     * Helper method to create transaction records with enhanced currency support
     */
    private void createTransactionRecord(MoneyBox moneyBox, TransactionType type, BigDecimal amount, 
                                       BigDecimal balanceBefore, String description, 
                                       String referenceId, String referenceType, Currency currency) {
        // Convert amount to SYP if it's not already in SYP
        BigDecimal amountInSYP = amount;
        BigDecimal exchangeRate = BigDecimal.ONE;
        Currency originalCurrency = currency != null ? currency : Currency.SYP;
        BigDecimal originalAmount = amount;
        
        if (!Currency.SYP.equals(originalCurrency)) {
            try {
                amountInSYP = exchangeRateService.convertToSYP(amount, originalCurrency);
                exchangeRate = exchangeRateService.getExchangeRate(originalCurrency, Currency.SYP);
                log.debug("Converted {} {} to {} SYP using rate: {}", 
                        amount, originalCurrency, amountInSYP, exchangeRate);
            } catch (Exception e) {
                log.warn("Failed to convert currency for transaction: {}. Using original amount.", e.getMessage());
                // Fallback: use original amount but mark as unconverted
                amountInSYP = amount;
                exchangeRate = BigDecimal.ZERO;
            }
        }
        
        MoneyBoxTransaction transaction = new MoneyBoxTransaction();
        transaction.setMoneyBox(moneyBox);
        transaction.setTransactionType(type);
        transaction.setAmount(amountInSYP);
        transaction.setOriginalCurrency(originalCurrency);
        transaction.setOriginalAmount(originalAmount);
        transaction.setConvertedCurrency(Currency.SYP);
        transaction.setConvertedAmount(amountInSYP);
        transaction.setExchangeRate(exchangeRate);
        transaction.setConversionTimestamp(LocalDateTime.now());
        transaction.setConversionSource("EXCHANGE_RATE_SERVICE");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore.add(amountInSYP));
        transaction.setDescription(description + 
                               (originalCurrency != Currency.SYP ? " (Converted from " + originalCurrency + ")" : ""));
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transaction.setCreatedBy(getCurrentUser().getId()); // Set user ID reference
        
        transactionRepository.save(transaction);
        log.debug("Created transaction record: type={}, amount={} {} -> {} SYP, description={}", 
                type, originalAmount, originalCurrency, amountInSYP, description);
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
