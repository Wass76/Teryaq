package com.Teryaq.purchase.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTORequest {
    private Long supplierId;
    private String currency;
    private List<PurchaseOrderItemDTORequest> items;
} 