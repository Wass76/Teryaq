package com.Teryaq.sale.repo;

import com.Teryaq.sale.entity.SaleInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleInvoiceItemRepository extends JpaRepository<SaleInvoiceItem, Long> {
    // يمكنك إضافة دوال بحث مخصصة هنا إذا احتجت
} 