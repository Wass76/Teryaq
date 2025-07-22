package com.Teryaq.product.dto;

import lombok.Data;

@Data
public class PurchaseOrderItemDTORequest {
    private Long productId;
    private Integer quantity;
    private Double price;
    private String barcode;
    private String productType; // 'MASTER' or 'PHARMACY'
} 