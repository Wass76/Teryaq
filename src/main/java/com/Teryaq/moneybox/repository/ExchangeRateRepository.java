package com.Teryaq.moneybox.repository;

import com.Teryaq.moneybox.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndIsActiveTrue(
        String fromCurrency, String toCurrency);
    
    List<ExchangeRate> findByIsActiveTrue();
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.fromCurrency = :fromCurrency " +
           "AND e.toCurrency = :toCurrency AND e.effectiveFrom <= :date " +
           "AND (e.effectiveTo IS NULL OR e.effectiveTo > :date) AND e.isActive = true")
    Optional<ExchangeRate> findEffectiveRate(@Param("fromCurrency") String fromCurrency,
                                           @Param("toCurrency") String toCurrency,
                                           @Param("date") LocalDateTime date);
}
