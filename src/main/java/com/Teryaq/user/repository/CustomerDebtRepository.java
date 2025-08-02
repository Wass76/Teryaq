package com.Teryaq.user.repository;

import com.Teryaq.user.entity.CustomerDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDebtRepository extends JpaRepository<CustomerDebt, Long> {

    /**
     * البحث عن ديون العميل
     */
    List<CustomerDebt> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * البحث عن جميع ديون العميل
     */
    List<CustomerDebt> findByCustomerId(Long customerId);

    /**
     * البحث عن ديون العميل حسب الحالة
     */
    List<CustomerDebt> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, String status);

    /**
     * إجمالي ديون العميل
     */
    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM CustomerDebt d WHERE d.customer.id = :customerId AND d.status = 'ACTIVE'")
    Float getTotalDebtByCustomerId(@Param("customerId") Long customerId);

    /**
     * الديون النشطة للعميل
     */
    @Query("SELECT d FROM CustomerDebt d WHERE d.customer.id = :customerId AND d.status = 'ACTIVE' ORDER BY d.dueDate ASC")
    List<CustomerDebt> getActiveDebtsByCustomerId(@Param("customerId") Long customerId);

    /**
     * الديون المتأخرة
     */
    @Query("SELECT d FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE' ORDER BY d.dueDate ASC")
    List<CustomerDebt> getOverdueDebts();

    /**
     * إجمالي الديون المتأخرة
     */
    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM CustomerDebt d WHERE d.dueDate < CURRENT_TIMESTAMP AND d.status = 'ACTIVE'")
    Float getTotalOverdueDebts();

    /**
     * الديون حسب الحالة
     */
    List<CustomerDebt> findByStatusOrderByCreatedAtDesc(String status);
} 