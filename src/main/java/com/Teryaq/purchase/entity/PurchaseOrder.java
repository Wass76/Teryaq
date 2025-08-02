package com.Teryaq.purchase.entity;

import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "purchase_order")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class PurchaseOrder extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private String currency; // SYP or USD

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private OrderStatus status; // قيد الانتظار, مكتمل, ملغى

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Override
    protected String getSequenceName() {
        return "purchase_order_id_seq";
    }
} 