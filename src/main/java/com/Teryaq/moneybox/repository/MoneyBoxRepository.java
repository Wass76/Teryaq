package com.Teryaq.moneybox.repository;

import com.Teryaq.moneybox.entity.MoneyBox;
import com.Teryaq.moneybox.Enum.MoneyBoxStatus;
import com.Teryaq.moneybox.Enum.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoneyBoxRepository extends JpaRepository<MoneyBox, Long> {
    
    Optional<MoneyBox> findByPharmacyIdAndBusinessDateAndStatus(
        Long pharmacyId, LocalDate businessDate, MoneyBoxStatus status);
    
    List<MoneyBox> findByPharmacyIdAndBusinessDateBetween(
        Long pharmacyId, LocalDate startDate, LocalDate endDate);
    
    List<MoneyBox> findByPharmacyIdAndPeriodTypeAndStatus(
        Long pharmacyId, PeriodType periodType, MoneyBoxStatus status);
    
    Optional<MoneyBox> findFirstByPharmacyIdAndPeriodTypeOrderByBusinessDateDesc(
        Long pharmacyId, PeriodType periodType);
    
    boolean existsByPharmacyIdAndBusinessDateAndStatus(
        Long pharmacyId, LocalDate businessDate, MoneyBoxStatus status);
    
    @Query("SELECT m FROM MoneyBox m WHERE m.pharmacyId = :pharmacyId AND m.status = :status ORDER BY m.businessDate DESC")
    List<MoneyBox> findRecentByPharmacyAndStatus(@Param("pharmacyId") Long pharmacyId, @Param("status") MoneyBoxStatus status);
}
