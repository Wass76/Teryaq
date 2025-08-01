package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import lombok.Data;

@Data
public class SaleInvoiceItemDTORequest {
    private Long stockItemId;
    private Integer quantity;
    private Float unitPrice; // سعر الوحدة (اختياري - إذا لم يتم تحديده سيتم أخذه من المخزون)
    
    // الخصم على هذا العنصر
    private DiscountType discountType;
    private Float discountValue;
    
    // للتوافق مع الـ JSON القديم
    private Float discount;
    
    // للتوافق مع الكود القديم
    public Float getDiscountValue() {
        if (discount != null) {
            return discount;
        }
        return discountValue != null ? discountValue : 0f;
    }
    
    public DiscountType getDiscountType() {
        return discountType != null ? discountType : DiscountType.FIXED_AMOUNT;
    }
} 