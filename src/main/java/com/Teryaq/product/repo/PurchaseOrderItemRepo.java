package com.Teryaq.product.repo;

import com.Teryaq.product.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepo extends JpaRepository<PurchaseOrderItem, Long> {
} 