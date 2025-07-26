package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.*;
import com.Teryaq.product.entity.PurchaseInvoice;
import com.Teryaq.product.entity.PurchaseInvoiceItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.product.entity.PharmacyProductTranslation;
import com.Teryaq.product.entity.MasterProductTranslation;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import com.Teryaq.product.Enum.ProductType;

@Component
public class PurchaseInvoiceMapper {
    public PurchaseInvoice toEntity(PurchaseInvoiceDTORequest dto, Supplier supplier, List<PurchaseInvoiceItem> items) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPurchaseOrder(null); // Set in service if needed
        invoice.setSupplier(supplier);
        invoice.setCurrency(dto.getCurrency());
        invoice.setItems(items.stream().peek(i -> i.setPurchaseInvoice(invoice)).collect(Collectors.toList()));
        return invoice;
    }

    public PurchaseInvoiceDTOResponse toResponse(PurchaseInvoice invoice, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts, String language) {
        PurchaseInvoiceDTOResponse dto = new PurchaseInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setPurchaseOrderId(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null);
        dto.setSupplierName(invoice.getSupplier().getName());
        dto.setCurrency(invoice.getCurrency());
        dto.setTotal(invoice.getTotal());
        dto.setItems(invoice.getItems().stream().map(item -> {
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

    public PurchaseInvoiceItem toItemEntity(PurchaseInvoiceItemDTORequest dto) {
        PurchaseInvoiceItem item = new PurchaseInvoiceItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setReceivedQty(dto.getReceivedQty());
        item.setBonusQty(dto.getBonusQty() != null ? dto.getBonusQty() : 0); // Default to 0 if null
        item.setInvoicePrice(dto.getInvoicePrice());
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