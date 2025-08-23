package com.Teryaq.moneybox.repository;

import com.Teryaq.moneybox.entity.MoneyBoxTransaction;
import com.Teryaq.moneybox.Enum.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoneyBoxTransactionRepository extends JpaRepository<MoneyBoxTransaction, Long> {
    
    List<MoneyBoxTransaction> findByMoneyBoxIdOrderByTransactionDateDesc(Long moneyBoxId);
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionType(
        Long moneyBoxId, TransactionType transactionType);
    
    List<MoneyBoxTransaction> findByReferenceTypeAndReferenceId(
        String referenceType, Long referenceId);
    
    @Query("SELECT COALESCE(SUM(t.amountInSYP), 0) FROM MoneyBoxTransaction t " +
           "WHERE t.moneyBoxId = :moneyBoxId AND t.transactionType = :transactionType")
    BigDecimal sumAmountByType(@Param("moneyBoxId") Long moneyBoxId, 
                              @Param("transactionType") TransactionType transactionType);
    
    @Query("SELECT t FROM MoneyBoxTransaction t WHERE t.moneyBoxId = :moneyBoxId " +
           "ORDER BY t.transactionDate DESC")
    List<MoneyBoxTransaction> findTransactionsByMoneyBox(@Param("moneyBoxId") Long moneyBoxId);
}
