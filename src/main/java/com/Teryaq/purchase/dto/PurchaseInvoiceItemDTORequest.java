package com.Teryaq.purchase.dto;

import java.time.LocalDate;

import com.Teryaq.product.Enum.ProductType;
import lombok.Data;

@Data
public class PurchaseInvoiceItemDTORequest {
    private Long productId;
    private Integer receivedQty;
    private Integer bonusQty;
    private Double invoicePrice;
    private String batchNo;
    private LocalDate expiryDate;
    private ProductType productType; // 'MASTER' or 'PHARMACY'
} 