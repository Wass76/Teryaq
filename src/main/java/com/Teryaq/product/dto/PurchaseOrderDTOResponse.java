package com.Teryaq.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTOResponse {
    private Long id;
    private String supplierName;
    private String currency;
    private Double total;
    private String status;
    private List<PurchaseOrderItemDTOResponse> items;
} 