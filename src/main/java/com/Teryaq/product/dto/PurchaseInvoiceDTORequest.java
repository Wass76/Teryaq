package com.Teryaq.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseInvoiceDTORequest {
    private Long purchaseOrderId;
    private Long supplierId;
    private String currency;
    private Double total;
    private String status;
    private List<PurchaseInvoiceItemDTORequest> items;
} 