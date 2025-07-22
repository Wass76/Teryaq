package com.Teryaq.product.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "purchase_invoice_item")
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceItem extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productType; // 'MASTER' or 'PHARMACY'

    @Column(nullable = false)
    private Integer receivedQty;

    @Column(nullable = false)
    private Integer bonusQty;

    @Column(nullable = false)
    private Double invoicePrice;

    @Column(nullable = false)
    private Double actualPrice;

    @Column(nullable = true)
    private String batchNo;

    @Column(nullable = true)
    private LocalDate expiryDate;

    @Override
    protected String getSequenceName() {
        return "purchase_invoice_item_id_seq";
    }
} 