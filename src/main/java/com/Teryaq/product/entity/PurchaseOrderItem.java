package com.Teryaq.product.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "purchase_order_item")
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productType; // 'MASTER' or 'PHARMACY'

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = true)
    private String barcode;

    @Override
    protected String getSequenceName() {
        return "purchase_order_item_id_seq";
    }
} 