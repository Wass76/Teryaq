package com.Teryaq.purchase.repository;

import com.Teryaq.purchase.entity.PurchaseOrder;
import com.Teryaq.product.Enum.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByStatus(OrderStatus status);
    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);
    List<PurchaseOrder> findByPharmacyId(Long pharmacyId);
    Page<PurchaseOrder> findByPharmacyId(Long pharmacyId, Pageable pageable);
    List<PurchaseOrder> findByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status);
    Page<PurchaseOrder> findByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status, Pageable pageable);
} 