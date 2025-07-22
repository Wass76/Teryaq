package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.*;
import com.Teryaq.product.entity.PurchaseOrder;
import com.Teryaq.product.entity.PurchaseOrderItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.user.entity.Supplier;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderMapper {
    public PurchaseOrder toEntity(PurchaseOrderDTORequest dto, Supplier supplier, List<PurchaseOrderItem> items) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setCurrency(dto.getCurrency());
        order.setStatus("قيد الانتظار");
        order.setItems(items.stream().peek(i -> i.setPurchaseOrder(order)).collect(Collectors.toSet()));
        double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotal(total);
        return order;
    }

    public PurchaseOrderDTOResponse toResponse(PurchaseOrder order, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts) {
        PurchaseOrderDTOResponse dto = new PurchaseOrderDTOResponse();
        dto.setId(order.getId());
        dto.setSupplierName(order.getSupplier().getName());
        dto.setCurrency(order.getCurrency());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setItems(order.getItems().stream().map(item -> {
            String productName = null;
            if ("PHARMACY".equals(item.getProductType())) {
                PharmacyProduct product = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = product != null ? product.getTradeName() : "N/A";
            } else if ("MASTER".equals(item.getProductType())) {
                MasterProduct product = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = product != null ? product.getTradeName() : "N/A";
            }
            return toItemResponse(item, productName);
        }).toList());
        return dto;
    }

    public PurchaseOrderItem toItemEntity(PurchaseOrderItemDTORequest dto) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        item.setBarcode(dto.getBarcode());
        return item;
    }

    public PurchaseOrderItemDTOResponse toItemResponse(PurchaseOrderItem item, String productName) {
        PurchaseOrderItemDTOResponse dto = new PurchaseOrderItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductName(productName);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setBarcode(item.getBarcode());
        return dto;
    }
} 