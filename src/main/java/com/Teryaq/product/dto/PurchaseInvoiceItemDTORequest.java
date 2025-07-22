package com.Teryaq.product.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PurchaseInvoiceItemDTORequest {
    private Long productId;
    private Integer receivedQty;
    private Integer bonusQty;
    private Double invoicePrice;
    private Double actualPrice;
    private String batchNo;
    private LocalDate expiryDate;
    private String productType; // 'MASTER' or 'PHARMACY'
} 