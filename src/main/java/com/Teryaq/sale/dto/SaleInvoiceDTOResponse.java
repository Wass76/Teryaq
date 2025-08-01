package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleInvoiceDTOResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    
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
