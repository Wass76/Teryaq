package com.Teryaq.purchase.repository;

import com.Teryaq.purchase.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Teryaq.product.Enum.ProductType;
import java.util.List;

public interface PurchaseOrderItemRepo extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByProductIdAndProductType(Long productId, ProductType productType);
} 