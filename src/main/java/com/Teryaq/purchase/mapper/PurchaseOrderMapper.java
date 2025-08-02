package com.Teryaq.purchase.mapper;

import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.purchase.dto.PurchaseOrderDTORequest;
import com.Teryaq.purchase.dto.PurchaseOrderDTOResponse;
import com.Teryaq.purchase.dto.PurchaseOrderItemDTORequest;
import com.Teryaq.purchase.dto.PurchaseOrderItemDTOResponse;
import com.Teryaq.purchase.entity.PurchaseOrder;
import com.Teryaq.purchase.entity.PurchaseOrderItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.user.entity.Supplier;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import com.Teryaq.product.entity.PharmacyProductTranslation;
import com.Teryaq.product.entity.MasterProductTranslation;

@Component
public class PurchaseOrderMapper {
    public PurchaseOrder toEntity(PurchaseOrderDTORequest dto, Supplier supplier, List<PurchaseOrderItem> items) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setCurrency(dto.getCurrency());
        order.setStatus(OrderStatus.PENDING);
        order.setItems(items.stream().peek(i -> i.setPurchaseOrder(order)).collect(Collectors.toList()));
        double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotal(total);
        return order;
    }

    public PurchaseOrderDTOResponse toResponse(PurchaseOrder order, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts, String language) {
        PurchaseOrderDTOResponse dto = new PurchaseOrderDTOResponse();
        dto.setId(order.getId());
        dto.setSupplierName(order.getSupplier().getName());
        dto.setCurrency(order.getCurrency());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setItems(order.getItems().stream().map(item -> {
            String productName = null;
            if (item.getProductType() == ProductType.PHARMACY) {
                PharmacyProduct product = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (product != null) {
                    // Try to get translated name
                    productName = product.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(PharmacyProductTranslation::getTradeName)
                        .orElse(product.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            } else if (item.getProductType() == ProductType.MASTER) {
                MasterProduct product = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (product != null) {
                    // Try to get translated name
                    productName = product.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(MasterProductTranslation::getTradeName)
                        .orElse(product.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            }
            return toItemResponse(item, productName);
        }).toList());
        return dto;
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
            if (item.getProductType() == ProductType.PHARMACY) {
                PharmacyProduct product = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = product != null ? product.getTradeName() : "N/A";
            } else if (item.getProductType() == ProductType.MASTER) {
                MasterProduct product = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = product != null ? product.getTradeName() : "N/A";
            }
            return toItemResponse(item, productName);
        }).toList());
        return dto;
    }

    public PurchaseOrderItem toItemEntity(PurchaseOrderItemDTORequest dto, String barcode, Double price) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setQuantity(dto.getQuantity());
        item.setPrice(price);
        item.setBarcode(barcode);
        return item;
    }

    public PurchaseOrderItemDTOResponse toItemResponse(PurchaseOrderItem item, String productName) {
        PurchaseOrderItemDTOResponse dto = new PurchaseOrderItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(productName);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setBarcode(item.getBarcode());
        dto.setProductType(item.getProductType());
        return dto;
    }
} 