package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleInvoiceItemDTOResponse {
    private Long id;
    private Long stockItemId;
    private String productName;
    private Integer quantity;
    private Float unitPrice;
    private Float discount;
    private DiscountType discountType;
    private Float subTotal;
} 