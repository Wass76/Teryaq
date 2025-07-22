package com.Teryaq.product.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PurchaseInvoiceItemDTOResponse {
    private Long id;
    private String productName;
    private Integer receivedQty;
    private Integer bonusQty;
    private Double invoicePrice;
    private Double actualPrice;
    private String batchNo;
    private LocalDate expiryDate;
} 