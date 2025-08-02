package com.Teryaq.sale.mapper;

import com.Teryaq.sale.dto.*;
import com.Teryaq.sale.entity.*;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.service.StockManagementService;
import com.Teryaq.user.entity.Customer;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SaleMapper {
    
    private final StockManagementService stockManagementService;
    
    public SaleInvoice toEntity(SaleInvoiceDTORequest dto) {
        SaleInvoice invoice = new SaleInvoice();
        invoice.setPaymentType(dto.getPaymentType());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setDiscount(dto.getInvoiceDiscountValue() != null ? dto.getInvoiceDiscountValue() : 0);
        invoice.setDiscountType(dto.getInvoiceDiscountType());
        invoice.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : 0);
        return invoice;
    }
    
    public SaleInvoice toEntityWithItems(SaleInvoiceDTORequest dto, List<SaleInvoiceItem> items) {
        SaleInvoice invoice = toEntity(dto);
        invoice.setItems(items);
        return invoice;
    }
    
    public SaleInvoice toEntityWithCustomerAndDate(SaleInvoiceDTORequest dto, Customer customer) {
        SaleInvoice invoice = toEntity(dto);
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(java.time.LocalDate.now());
        return invoice;
    }
    
    public SaleInvoiceItem toEntity(SaleInvoiceItemDTORequest dto, StockItem stockItem) {
        SaleInvoiceItem item = new SaleInvoiceItem();
        item.setStockItem(stockItem);
        item.setQuantity(dto.getQuantity());
        item.setDiscount(dto.getDiscountValue() != null ? dto.getDiscountValue() : 0);
        item.setDiscountType(dto.getDiscountType());
        
        // تعيين سعر الوحدة
        if (dto.getUnitPrice() != null) {
            item.setUnitPrice(dto.getUnitPrice());
        } else {
            // استخدام سعر الشراء الفعلي كسعر البيع
            item.setUnitPrice(stockItem.getActualPurchasePrice().floatValue());
        }
        
        return item;
    }
    
    public List<SaleInvoiceItem> toEntityList(List<SaleInvoiceItemDTORequest> dtos, List<StockItem> stockItems) {
        return dtos.stream()
            .map(dto -> {
                StockItem stockItem = stockItems.stream()
                    .filter(stock -> stock.getId().equals(dto.getStockItemId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Stock item not found with ID: " + dto.getStockItemId()));
                return toEntity(dto, stockItem);
            })
            .collect(Collectors.toList());
    }

    public SaleInvoiceItemDTOResponse toResponse(SaleInvoiceItem item) {
        SaleInvoiceItemDTOResponse dto = new SaleInvoiceItemDTOResponse();
        dto.setId(item.getId());
        dto.setStockItemId(item.getStockItem() != null ? item.getStockItem().getId() : null);
        
        // إضافة اسم المنتج باستخدام StockManagementService
        String productName = "Unknown Product";
        if (item.getStockItem() != null) {
            productName = stockManagementService.getProductName(
                item.getStockItem().getProductId(), 
                item.getStockItem().getProductType()
            );
        }
        dto.setProductName(productName);
        
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setDiscountType(item.getDiscountType());
        dto.setSubTotal(item.getSubTotal());
        return dto;
    }

    public SaleInvoiceDTOResponse toResponse(SaleInvoice invoice) {
        SaleInvoiceDTOResponse dto = new SaleInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setCustomerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null);
        dto.setCustomerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "cash customer");
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaymentType(invoice.getPaymentType());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setDiscount(invoice.getDiscount());
        dto.setDiscountType(invoice.getDiscountType());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setRemainingAmount(invoice.getRemainingAmount());
        // حساب الحالة بناءً على المبلغ المتبقي
        String status = invoice.getRemainingAmount() > 0 ? "PENDING" : "COMPLETED";
        dto.setStatus(status);
        if (invoice.getItems() != null) {
            List<SaleInvoiceItemDTOResponse> items = invoice.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            dto.setItems(items);
        }
        return dto;
    }
} 