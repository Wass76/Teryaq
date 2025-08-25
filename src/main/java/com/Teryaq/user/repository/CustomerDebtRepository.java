package com.Teryaq.user.repository;

import com.Teryaq.user.entity.CustomerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, Long> {

    List<CustomerDebt> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<CustomerDebt> findByCustomerId(Long customerId);

    List<CustomerDebt> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, String status);

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM CustomerDebt d WHERE d.customer.id = :customerId AND d.status = 'ACTIVE'")
    Float getTotalDebtByCustomerId(@Param("customerId") Long customerId);

  
    @Query("SELECT d FROM CustomerDebt d WHERE d.customer.id = :customerId AND d.status = 'ACTIVE' ORDER BY d.dueDate ASC")
    List<CustomerDebt> getActiveDebtsByCustomerId(@Param("customerId") Long customerId);

   
    @Query("SELECT d FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE' ORDER BY d.dueDate ASC")
    List<CustomerDebt> getOverdueDebts();


    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE'")
    Float getTotalOverdueDebts();

  
    List<CustomerDebt> findByStatusOrderByCreatedAtDesc(String status);



    @Query("SELECT d FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE' AND d.customer.pharmacy.id = :pharmacyId ORDER BY d.dueDate ASC")
    List<CustomerDebt> getOverdueDebtsByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE' AND d.customer.pharmacy.id = :pharmacyId")
    Float getTotalOverdueDebtsByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT d FROM CustomerDebt d WHERE d.status = :status AND d.customer.pharmacy.id = :pharmacyId ORDER BY d.createdAt DESC")
    List<CustomerDebt> findByStatusAndPharmacyIdOrderByCreatedAtDesc(@Param("status") String status, @Param("pharmacyId") Long pharmacyId);

    @Query("SELECT d FROM CustomerDebt d WHERE d.createdAt >= :startDate AND d.createdAt <= :endDate AND d.customer.pharmacy.id = :pharmacyId ORDER BY d.createdAt DESC")
    List<CustomerDebt> findByDateRangeAndPharmacyId(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate, @Param("pharmacyId") Long pharmacyId);

    @Query("SELECT d FROM CustomerDebt d WHERE d.amount >= :minAmount AND d.amount <= :maxAmount AND d.customer.pharmacy.id = :pharmacyId ORDER BY d.amount DESC")
    List<CustomerDebt> findByAmountRangeAndPharmacyId(@Param("minAmount") Float minAmount, @Param("maxAmount") Float maxAmount, @Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COUNT(d), " +
           "SUM(CASE WHEN d.status = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN d.status = 'PAID' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(d.amount), 0), " +
           "COALESCE(SUM(d.paidAmount), 0), " +
           "COALESCE(SUM(d.remainingAmount), 0) " +
           "FROM CustomerDebt d WHERE d.customer.pharmacy.id = :pharmacyId")
    Object[] getDebtStatisticsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
} 