package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderDTORequest {
    private Long supplierId;
    private Currency currency;
    private List<PurchaseOrderItemDTORequest> items;
} 