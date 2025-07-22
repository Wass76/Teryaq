package com.Teryaq.product.repo;

import com.Teryaq.product.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceRepo extends JpaRepository<PurchaseInvoice, Long> {
} 