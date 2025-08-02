package com.Teryaq.purchase.repository;

import com.Teryaq.purchase.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepo extends JpaRepository<PurchaseOrderItem, Long> {
} 