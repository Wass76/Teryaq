package com.Teryaq.sale.repo;

import com.Teryaq.sale.entity.SaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, Long> {
    // يمكنك إضافة دوال بحث مخصصة هنا إذا احتجت
}
