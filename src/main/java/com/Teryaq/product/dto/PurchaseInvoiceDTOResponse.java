package com.Teryaq.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseInvoiceDTOResponse {
    private Long id;
    private Long purchaseOrderId;
    private String supplierName;
    private String currency;
    private Double total;
    private String status;
    private List<PurchaseInvoiceItemDTOResponse> items;
} 