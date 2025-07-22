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
@Table(name = "purchase_order")
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private String currency; // SYP or USD

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private String status; // قيد الانتظار, مكتمل, ملغى

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PurchaseOrderItem> items = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "purchase_order_id_seq";
    }
} 