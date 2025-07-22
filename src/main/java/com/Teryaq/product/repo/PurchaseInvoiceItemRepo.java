package com.Teryaq.product.repo;

import com.Teryaq.product.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceItemRepo extends JpaRepository<PurchaseInvoiceItem, Long> {
} 