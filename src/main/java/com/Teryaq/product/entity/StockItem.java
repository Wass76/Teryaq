package com.Teryaq.product.entity;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "stock_item")
@NoArgsConstructor
@AllArgsConstructor
public class StockItem extends AuditedEntity {
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private ProductType productType; // 'MASTER' or 'PHARMACY'

    @Column(nullable = false)
    private Integer quantity;

    @Column
    private Integer bonusQty;

    @Column
    private LocalDate expiryDate;

    @Column
    private String batchNo;

    @Column(nullable = false)
    private Double actualPurchasePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id")
    private PurchaseInvoice purchaseInvoice;

    @Column
    private LocalDate dateAdded;

    @Column
    private Long addedBy;

    @Override
    protected String getSequenceName() {
        return "stock_item_id_seq";
    }
} 