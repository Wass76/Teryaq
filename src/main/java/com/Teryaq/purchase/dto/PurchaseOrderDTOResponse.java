package com.Teryaq.purchase.dto;

import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.user.Enum.Currency;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTOResponse {
    private Long id;
    private String supplierName;
    private Currency currency;
    private Double total;
    private OrderStatus status;
    private List<PurchaseOrderItemDTOResponse> items;
} 