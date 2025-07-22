package com.Teryaq.product.entity;

import com.Teryaq.user.entity.Supplier;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "purchase_invoice")
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoice extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private String status; // مكتمل, ملغى

    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PurchaseInvoiceItem> items = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "purchase_invoice_id_seq";
    }
} 