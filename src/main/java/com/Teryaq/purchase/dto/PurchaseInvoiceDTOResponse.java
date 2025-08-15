package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseInvoiceDTOResponse {
    private Long id;
    private Long purchaseOrderId;
    private Long supplierId;
    private String supplierName;
    private Currency currency;
    private Double total;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private Long createdBy;
    private List<PurchaseInvoiceItemDTOResponse> items;
} 