package com.Teryaq.purchase.mapper;

import com.Teryaq.purchase.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.purchase.dto.PurchaseInvoiceItemDTORequest;
import com.Teryaq.purchase.dto.PurchaseInvoiceItemDTOResponse;
import com.Teryaq.purchase.entity.PurchaseInvoice;
import com.Teryaq.purchase.entity.PurchaseInvoiceItem;
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
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setItems(items.stream().peek(i -> i.setPurchaseInvoice(invoice)).collect(Collectors.toList()));
        // Note: pharmacy will be set in the service layer
        return invoice;
    }

    public PurchaseInvoiceDTOResponse toResponse(PurchaseInvoice invoice, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts, String language) {
        PurchaseInvoiceDTOResponse dto = new PurchaseInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setPurchaseOrderId(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null);
        dto.setSupplierId(invoice.getSupplier().getId());
        dto.setSupplierName(invoice.getSupplier().getName());
        dto.setCurrency(invoice.getCurrency());
        dto.setTotal(invoice.getTotal());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setItems(invoice.getItems().stream().map(item -> {
            String productName = null;
            PharmacyProduct pharmacyProduct = null;
            MasterProduct masterProduct = null;
            
            if (item.getProductType() == ProductType.PHARMACY) {
                pharmacyProduct = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (pharmacyProduct != null) {
                    // Try to get translated name
                    productName = pharmacyProduct.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(PharmacyProductTranslation::getTradeName)
                        .orElse(pharmacyProduct.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            } else if (item.getProductType() == ProductType.MASTER) {
                masterProduct = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (masterProduct != null) {
                    // Try to get translated name
                    productName = masterProduct.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(MasterProductTranslation::getTradeName)
                        .orElse(masterProduct.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            }
            return toItemResponse(item, productName, pharmacyProduct, masterProduct);
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

    public PurchaseInvoiceItemDTOResponse toItemResponse(PurchaseInvoiceItem item, String productName, PharmacyProduct pharmacyProduct, MasterProduct masterProduct) {
        PurchaseInvoiceItemDTOResponse dto = new PurchaseInvoiceItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductName(productName);
        dto.setReceivedQty(item.getReceivedQty());
        dto.setBonusQty(item.getBonusQty());
        dto.setInvoicePrice(item.getInvoicePrice());
        dto.setActualPrice(item.getActualPrice());
        dto.setBatchNo(item.getBatchNo());
        dto.setExpiryDate(item.getExpiryDate());
        
        // Set refSellingPrice and minStockLevel based on product type
        if (item.getProductType() == ProductType.PHARMACY && pharmacyProduct != null) {
            dto.setRefSellingPrice((double) pharmacyProduct.getRefSellingPrice());
            dto.setMinStockLevel(pharmacyProduct.getMinStockLevel());
        } else if (item.getProductType() == ProductType.MASTER && masterProduct != null) {
            dto.setRefSellingPrice((double) masterProduct.getRefSellingPrice());
            // TODO: Implement minStockLevel for MASTER type products after fixing related issues
            dto.setMinStockLevel(null);
        } else {
            dto.setRefSellingPrice(null);
            dto.setMinStockLevel(null);
        }
        
        return dto;
    }
} 