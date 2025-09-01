package com.Teryaq.sale.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentMethod;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.sale.enums.InvoiceStatus;
import com.Teryaq.sale.enums.PaymentStatus;
import com.Teryaq.sale.enums.RefundStatus;
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

    @Column(unique = true)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss")
    private LocalDateTime invoiceDate;

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
    private Currency currency = Currency.SYP; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "saleInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleInvoiceItem> items;

    // حالة الفاتورة الأساسية: SOLD, CANCELLED, VOID
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.SOLD;
    
    // حالة الدفع: FULLY_PAID, PARTIALLY_PAID, UNPAID
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.FULLY_PAID;
    
    // حالة المرتجعات: NO_REFUND, PARTIALLY_REFUNDED, FULLY_REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus = RefundStatus.NO_REFUND;

    @Override
    protected String getSequenceName() {
        return "sale_invoice_id_seq";
    }


} 