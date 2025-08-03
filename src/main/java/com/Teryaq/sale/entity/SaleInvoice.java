package com.Teryaq.sale.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentMethod;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.utils.entity.AuditedEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sale_invoices")
public class SaleInvoice extends AuditedEntity{

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;

    private float totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private float discount;
    
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private float paidAmount;

    private float remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency = Currency.SYP; // افتراضي الليرة السورية

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "saleInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleInvoiceItem> items;

    @Override
    protected String getSequenceName() {
        return "sale_invoice_id_seq";
    }


} 