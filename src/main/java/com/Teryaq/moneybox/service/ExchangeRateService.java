package com.Teryaq.moneybox.service;

import com.Teryaq.moneybox.repository.ExchangeRateRepository;
import com.Teryaq.moneybox.entity.ExchangeRate;
import com.Teryaq.moneybox.dto.ExchangeRateResponseDTO;
import com.Teryaq.moneybox.dto.CurrencyConversionResponseDTO;
import com.Teryaq.moneybox.mapper.ExchangeRateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    
    /**
     * Get current exchange rate for currency pair
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        // First try to get from database
        Optional<ExchangeRate> dbRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency);
        if (dbRate.isPresent()) {
            return dbRate.get().getRate();
        }
        
        // Fallback to fixed rates if no database record
        if (fromCurrency.equals("SYP") && toCurrency.equals("USD")) {
            return new BigDecimal("0.0004"); // 1 USD = 2500 SYP (approximate)
        }
        if (fromCurrency.equals("USD") && toCurrency.equals("SYP")) {
            return new BigDecimal("2500");
        }
        if (fromCurrency.equals("SYP") && toCurrency.equals("EUR")) {
            return new BigDecimal("0.00037"); // 1 EUR = 2700 SYP (approximate)
        }
        if (fromCurrency.equals("EUR") && toCurrency.equals("SYP")) {
            return new BigDecimal("2700");
        }
        
        throw new IllegalArgumentException("Unsupported currency pair: " + fromCurrency + " to " + toCurrency);
    }
    
    /**
     * Convert amount from one currency to another
     */
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Convert amount to SYP (base currency)
     */
    public BigDecimal convertToSYP(BigDecimal amount, String currency) {
        return convertAmount(amount, currency, "SYP");
    }
    
    /**
     * Convert amount from SYP to target currency
     */
    public BigDecimal convertFromSYP(BigDecimal amount, String targetCurrency) {
        return convertAmount(amount, "SYP", targetCurrency);
    }
    
    /**
     * Get effective exchange rate from database
     */
    public Optional<ExchangeRate> getEffectiveRate(String fromCurrency, String toCurrency, LocalDateTime date) {
        return exchangeRateRepository.findEffectiveRate(fromCurrency, toCurrency, date);
    }
    
    /**
     * Get current active exchange rate from database
     */
    public ExchangeRateResponseDTO getCurrentRate(String fromCurrency, String toCurrency) {
        ExchangeRate rate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency)
            .orElseThrow(() -> new IllegalArgumentException("No active exchange rate found for " + fromCurrency + " to " + toCurrency));
        
        return exchangeRateMapper.toResponse(rate);
    }
    
    /**
     * Set a new exchange rate
     */
    @Transactional
    public ExchangeRateResponseDTO setExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, String source, String notes) {
        // Deactivate any existing active rates for this currency pair
        Optional<ExchangeRate> existingRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency);
        if (existingRate.isPresent()) {
            ExchangeRate oldRate = existingRate.get();
            oldRate.setIsActive(false);
            oldRate.setEffectiveTo(LocalDateTime.now());
            exchangeRateRepository.save(oldRate);
        }
        
        // Create new exchange rate
        ExchangeRate newRate = ExchangeRate.builder()
            .fromCurrency(fromCurrency)
            .toCurrency(toCurrency)
            .rate(rate)
            .effectiveFrom(LocalDateTime.now())
            .isActive(true)
            .source(source != null ? source : "MANUAL")
            .notes(notes)
            .build();
        
        ExchangeRate savedRate = exchangeRateRepository.save(newRate);
        return exchangeRateMapper.toResponse(savedRate);
    }
    
    /**
     * Get all active exchange rates
     */
    public List<ExchangeRateResponseDTO> getAllActiveRates() {
        List<ExchangeRate> rates = exchangeRateRepository.findByIsActiveTrue();
        return rates.stream()
            .map(exchangeRateMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get exchange rate by ID
     */
    public ExchangeRateResponseDTO getRateById(Long id) {
        ExchangeRate rate = exchangeRateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Exchange rate not found with ID: " + id));
        
        return exchangeRateMapper.toResponse(rate);
    }
    
    /**
     * Deactivate an exchange rate
     */
    @Transactional
    public void deactivateRate(Long id) {
        ExchangeRate rate = exchangeRateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Exchange rate not found with ID: " + id));
        
        rate.setIsActive(false);
        rate.setEffectiveTo(LocalDateTime.now());
        exchangeRateRepository.save(rate);
    }
    
    /**
     * Convert amount between currencies and return DTO
     */
    public CurrencyConversionResponseDTO convertAmountToDTO(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal convertedAmount = convertAmount(amount, fromCurrency, toCurrency);
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        return exchangeRateMapper.toConversionResponse(amount, fromCurrency, convertedAmount, toCurrency, rate);
    }
}
