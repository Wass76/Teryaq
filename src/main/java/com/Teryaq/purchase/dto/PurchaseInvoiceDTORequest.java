package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseInvoiceDTORequest {
    private Long purchaseOrderId;
    private Long supplierId;
    private Currency currency;
    private Double total;
    private List<PurchaseInvoiceItemDTORequest> items;
} 