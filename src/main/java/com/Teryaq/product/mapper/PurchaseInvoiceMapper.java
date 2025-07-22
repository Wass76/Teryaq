package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.*;
import com.Teryaq.product.entity.PurchaseInvoice;
import com.Teryaq.product.entity.PurchaseInvoiceItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.user.entity.Supplier;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurchaseInvoiceMapper {
    public PurchaseInvoice toEntity(PurchaseInvoiceDTORequest dto, Supplier supplier, List<PurchaseInvoiceItem> items) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPurchaseOrder(null); // Set in service if needed
        invoice.setSupplier(supplier);
        invoice.setCurrency(dto.getCurrency());
        invoice.setStatus(dto.getStatus());
        invoice.setItems(items.stream().peek(i -> i.setPurchaseInvoice(invoice)).collect(Collectors.toSet()));
        invoice.setTotal(dto.getTotal());
        return invoice;
    }

    public PurchaseInvoiceDTOResponse toResponse(PurchaseInvoice invoice, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts) {
        PurchaseInvoiceDTOResponse dto = new PurchaseInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setPurchaseOrderId(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null);
        dto.setSupplierName(invoice.getSupplier().getName());
        dto.setCurrency(invoice.getCurrency());
        dto.setTotal(invoice.getTotal());
        dto.setStatus(invoice.getStatus());
        dto.setItems(invoice.getItems().stream().map(item -> {
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

    public PurchaseInvoiceItem toItemEntity(PurchaseInvoiceItemDTORequest dto) {
        PurchaseInvoiceItem item = new PurchaseInvoiceItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setReceivedQty(dto.getReceivedQty());
        item.setBonusQty(dto.getBonusQty());
        item.setInvoicePrice(dto.getInvoicePrice());
        item.setActualPrice(dto.getActualPrice());
        item.setBatchNo(dto.getBatchNo());
        item.setExpiryDate(dto.getExpiryDate());
        return item;
    }

    public PurchaseInvoiceItemDTOResponse toItemResponse(PurchaseInvoiceItem item, String productName) {
        PurchaseInvoiceItemDTOResponse dto = new PurchaseInvoiceItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductName(productName);
        dto.setReceivedQty(item.getReceivedQty());
        dto.setBonusQty(item.getBonusQty());
        dto.setInvoicePrice(item.getInvoicePrice());
        dto.setActualPrice(item.getActualPrice());
        dto.setBatchNo(item.getBatchNo());
        dto.setExpiryDate(item.getExpiryDate());
        return dto;
    }
} 