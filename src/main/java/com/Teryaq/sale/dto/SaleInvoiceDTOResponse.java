package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleInvoiceDTOResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDateTime invoiceDate;
    private float totalAmount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private float discount;
    private DiscountType discountType;
    private float paidAmount;
    private float remainingAmount;
    private String status;
    private List<SaleInvoiceItemDTOResponse> items;
}
