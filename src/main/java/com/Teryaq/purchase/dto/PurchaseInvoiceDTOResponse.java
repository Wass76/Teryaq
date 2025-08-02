package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseInvoiceDTOResponse {
    private Long id;
    private Long purchaseOrderId;
    private String supplierName;
    private Currency currency;
    private Double total;
    private List<PurchaseInvoiceItemDTOResponse> items;
} 