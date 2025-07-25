package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import lombok.Data;

@Data
public class PurchaseOrderItemDTORequest {
    private Long productId;
    private Integer quantity;
    private Double price;
    private String barcode;
    private ProductType productType; // 'MASTER' or 'PHARMACY'
} 