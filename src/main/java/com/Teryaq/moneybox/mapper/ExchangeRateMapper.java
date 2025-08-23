package com.Teryaq.moneybox.mapper;

import com.Teryaq.moneybox.dto.ExchangeRateRequestDTO;
import com.Teryaq.moneybox.dto.ExchangeRateResponseDTO;
import com.Teryaq.moneybox.dto.CurrencyConversionResponseDTO;
import com.Teryaq.moneybox.entity.ExchangeRate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExchangeRateMapper {

    public ExchangeRateResponseDTO toResponse(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }

        return ExchangeRateResponseDTO.builder()
            .id(exchangeRate.getId())
            .fromCurrency(exchangeRate.getFromCurrency())
            .toCurrency(exchangeRate.getToCurrency())
            .rate(exchangeRate.getRate())
            .effectiveFrom(exchangeRate.getEffectiveFrom())
            .effectiveTo(exchangeRate.getEffectiveTo())
            .isActive(exchangeRate.getIsActive())
            .source(exchangeRate.getSource())
            .notes(exchangeRate.getNotes())
            .createdAt(exchangeRate.getCreatedAt())
            .updatedAt(exchangeRate.getUpdatedAt())
            .build();
    }

    public CurrencyConversionResponseDTO toConversionResponse(BigDecimal fromAmount, String fromCurrency, 
                                                           BigDecimal toAmount, String toCurrency, BigDecimal rate) {
        return CurrencyConversionResponseDTO.builder()
            .fromAmount(fromAmount)
            .fromCurrency(fromCurrency)
            .toAmount(toAmount)
            .toCurrency(toCurrency)
            .rate(rate)
            .build();
    }
}
