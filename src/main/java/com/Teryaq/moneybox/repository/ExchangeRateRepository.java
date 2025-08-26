package com.Teryaq.moneybox.repository;

import com.Teryaq.moneybox.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndIsActiveTrue(String fromCurrency, String toCurrency);
    
    List<ExchangeRate> findByFromCurrencyAndIsActiveTrue(String fromCurrency);
    
    List<ExchangeRate> findByToCurrencyAndIsActiveTrue(String toCurrency);
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.isActive = true AND (e.fromCurrency = :currency OR e.toCurrency = :currency)")
    List<ExchangeRate> findActiveRatesByCurrency(@Param("currency") String currency);
    
    @Query("SELECT COUNT(e) FROM ExchangeRate e WHERE e.isActive = true AND e.fromCurrency = :fromCurrency AND e.toCurrency = :toCurrency")
    Long countActiveRates(@Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency);
}
