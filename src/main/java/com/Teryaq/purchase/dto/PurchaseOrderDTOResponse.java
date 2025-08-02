package com.Teryaq.purchase.dto;

import com.Teryaq.product.Enum.OrderStatus;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTOResponse {
    private Long id;
    private String supplierName;
    private String currency;
    private Double total;
    private OrderStatus status;
    private List<PurchaseOrderItemDTOResponse> items;
} 