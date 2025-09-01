package com.Teryaq.sale.repo;

import com.Teryaq.sale.entity.SaleRefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRefundItemRepo extends JpaRepository<SaleRefundItem, Long> {
    
    List<SaleRefundItem> findBySaleRefundId(Long saleRefundId);
    
    List<SaleRefundItem> findBySaleInvoiceItemId(Long saleInvoiceItemId);
}
