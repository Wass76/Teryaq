package com.Teryaq.purchase.repository;

import com.Teryaq.purchase.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceItemRepo extends JpaRepository<PurchaseInvoiceItem, Long> {
} 