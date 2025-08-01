package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import lombok.Data;
import java.util.List;

@Data
public class SaleInvoiceDTORequest {
    private Long customerId;        
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    
    // الخصم على الفاتورة كاملة
    private DiscountType invoiceDiscountType;
    private Float invoiceDiscountValue;
    
    // للتوافق مع الـ JSON القديم
    private Float discount;
    
    private Float paidAmount;
    private List<SaleInvoiceItemDTORequest> items;
    
    // للتوافق مع الكود القديم
    public Float getInvoiceDiscountValue() {
        if (discount != null) {
            return discount;
        }
        return invoiceDiscountValue != null ? invoiceDiscountValue : 0f;
    }
    
    public DiscountType getInvoiceDiscountType() {
        return invoiceDiscountType != null ? invoiceDiscountType : DiscountType.FIXED_AMOUNT;
    }
} 