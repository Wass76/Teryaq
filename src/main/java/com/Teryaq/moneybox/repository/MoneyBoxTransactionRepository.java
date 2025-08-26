package com.Teryaq.moneybox.repository;

import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoneyBoxTransactionRepository extends JpaRepository<MoneyBoxTransaction, Long> {
    
    List<MoneyBoxTransaction> findByMoneyBoxIdOrderByCreatedAtDesc(Long moneyBoxId);
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
            Long moneyBoxId, TransactionType transactionType);
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType")
    BigDecimal getTotalAmountByType(@Param("moneyBoxId") Long moneyBoxId, @Param("transactionType") TransactionType transactionType);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByPeriod(@Param("moneyBoxId") Long moneyBoxId, 
                                     @Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByTypeAndPeriod(@Param("moneyBoxId") Long moneyBoxId, 
                                            @Param("transactionType") TransactionType transactionType,
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType")
    Long countTransactionsByType(@Param("moneyBoxId") Long moneyBoxId, @Param("transactionType") TransactionType transactionType);
    
    List<MoneyBoxTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
}
