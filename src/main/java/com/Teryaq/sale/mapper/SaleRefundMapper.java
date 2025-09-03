package com.Teryaq.sale.mapper;

import com.Teryaq.sale.dto.SaleRefundDTORequest;
import com.Teryaq.sale.dto.SaleRefundDTOResponse;
import com.Teryaq.sale.dto.SaleRefundItemDTOResponse;
import com.Teryaq.sale.entity.SaleRefund;
import com.Teryaq.sale.entity.SaleRefundItem;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.product.mapper.StockItemMapper;

@Component
public class SaleRefundMapper {
    
    private final StockItemMapper stockItemMapper;

    public SaleRefundMapper(StockItemMapper stockItemMapper) {
        this.stockItemMapper = stockItemMapper;
    }

    public SaleRefund toEntity(SaleRefundDTORequest request, SaleInvoice saleInvoice, Pharmacy pharmacy) {
        SaleRefund refund = new SaleRefund();
        refund.setSaleInvoice(saleInvoice);
        refund.setPharmacy(pharmacy);
        refund.setRefundReason(request.getRefundReason());
        refund.setRefundDate(LocalDateTime.now());
        refund.setStockRestored(false);
        return refund;
    }
    
    public SaleRefundDTOResponse toResponse(SaleRefund refund) {
        SaleInvoice saleInvoice = refund.getSaleInvoice();
        return SaleRefundDTOResponse.builder()
                .refundId(refund.getId())
                .saleInvoiceId(saleInvoice.getId())
                .totalRefundAmount(refund.getTotalRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundDate(refund.getRefundDate())
                .stockRestored(refund.getStockRestored())
                // معلومات العميل
                .customerId(saleInvoice.getCustomer().getId())
                .customerName(saleInvoice.getCustomer().getName())
                // معلومات الفاتورة الأصلية
                .originalInvoiceAmount(saleInvoice.getTotalAmount())
                .originalInvoicePaidAmount(saleInvoice.getPaidAmount())
                .originalInvoiceRemainingAmount(saleInvoice.getRemainingAmount())
                .paymentType(saleInvoice.getPaymentType().toString())
                .paymentMethod(saleInvoice.getPaymentMethod().toString())
                .currency(saleInvoice.getCurrency().toString())
                .refundedItems(refund.getRefundItems() != null ? 
                    refund.getRefundItems().stream()
                        .map(this::toRefundedItemResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }
    
    public SaleRefundDTOResponse toResponseWithDebtInfo(SaleRefund refund, Float customerTotalDebt, Integer customerActiveDebtsCount) {
        SaleInvoice saleInvoice = refund.getSaleInvoice();
        return SaleRefundDTOResponse.builder()
                .refundId(refund.getId())
                .saleInvoiceId(saleInvoice.getId())
                .totalRefundAmount(refund.getTotalRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundDate(refund.getRefundDate())
                .stockRestored(refund.getStockRestored())
                .customerId(saleInvoice.getCustomer().getId())
                .customerName(saleInvoice.getCustomer().getName())
                .originalInvoiceAmount(saleInvoice.getTotalAmount())
                .originalInvoicePaidAmount(saleInvoice.getPaidAmount())
                .originalInvoiceRemainingAmount(saleInvoice.getRemainingAmount())
                .paymentType(saleInvoice.getPaymentType().toString())
                .paymentMethod(saleInvoice.getPaymentMethod().toString())
                .currency(saleInvoice.getCurrency().toString())
                .customerTotalDebt(customerTotalDebt)
                .customerActiveDebtsCount(customerActiveDebtsCount)
                .refundedItems(refund.getRefundItems() != null ? 
                    refund.getRefundItems().stream()
                        .map(this::toRefundedItemResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }
    
    public SaleRefundItemDTOResponse toRefundedItemResponse(SaleRefundItem refundItem) {
        return SaleRefundItemDTOResponse.builder()
                .productName(getProductName(refundItem.getSaleInvoiceItem()))
                .quantity(refundItem.getRefundQuantity())
                .unitPrice(refundItem.getUnitPrice())
                .subtotal(refundItem.getSubtotal())
                .itemRefundReason(refundItem.getItemRefundReason())
                .build();
    }
    
    private String getProductName(SaleInvoiceItem item) {
        if (item.getStockItem() != null) {
            return "Product ID: " + item.getStockItem().getProductName();
        }
        return "Unknown Product";
    }

    public Map<String, Object> toRefundDetailsMap(SaleRefund refund, float customerTotalDebt, int customerActiveDebtsCount) {
        SaleInvoice saleInvoice = refund.getSaleInvoice();
        Customer customer = saleInvoice.getCustomer();
        
        Map<String, Object> result = new HashMap<>();
        result.put("refundId", refund.getId());
        result.put("saleInvoiceId", saleInvoice.getId());
        result.put("customerId", customer.getId());
        result.put("customerName", customer.getName());
        result.put("totalRefundAmount", refund.getTotalRefundAmount());
        result.put("refundType", determineRefundType(saleInvoice, refund.getTotalRefundAmount()));
        result.put("cashRefundAmount", calculateCashRefund(saleInvoice, refund.getTotalRefundAmount()));
        result.put("debtReductionAmount", calculateDebtReduction(saleInvoice, refund.getTotalRefundAmount()));
        result.put("originalInvoicePaidAmount", saleInvoice.getPaidAmount());
        result.put("originalInvoiceRemainingAmount", saleInvoice.getRemainingAmount());
        result.put("paymentType", saleInvoice.getPaymentType());
        result.put("paymentMethod", saleInvoice.getPaymentMethod());
        result.put("currency", saleInvoice.getCurrency());
        result.put("refundReason", refund.getRefundReason());
        result.put("refundDate", refund.getRefundDate() != null ? 
            refund.getRefundDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss")) : null);
        result.put("stockRestored", refund.getStockRestored());
        result.put("customerTotalDebt", customerTotalDebt);
        result.put("activeDebtsCount", customerActiveDebtsCount);
        result.put("refundedItems", refund.getRefundItems() != null ? 
            refund.getRefundItems().stream()
                .map(this::toRefundItemDetailsMap)
                .collect(Collectors.toList()) : List.of());
        
        return result;
    }
    
    private Map<String, Object> toRefundItemDetailsMap(SaleRefundItem refundItem) {
        Map<String, Object> details = new HashMap<>();
        details.put("quantity", refundItem.getRefundQuantity());
        details.put("unitPrice", refundItem.getUnitPrice());
        details.put("subtotal", refundItem.getSubtotal());
        details.put("itemRefundReason", refundItem.getItemRefundReason());
        details.put("stockRestored", refundItem.getStockRestored());
        
       
        try {
            String productName = stockItemMapper.getProductName(
                refundItem.getSaleInvoiceItem().getStockItem().getProductId(),
                refundItem.getSaleInvoiceItem().getStockItem().getProductType()
            );
            details.put("productName", productName);
        } catch (Exception e) {
            details.put("productName", "Unknown Product");
        }
        
        return details;
    }
    
    private String determineRefundType(SaleInvoice saleInvoice, float totalRefundAmount) {
        float paidAmount = saleInvoice.getPaidAmount();
        
        if (saleInvoice.getPaymentType() == PaymentType.CASH && saleInvoice.getRemainingAmount() == 0) {
            return "FULL_CASH_REFUND";
        } else if (saleInvoice.getPaymentType() == PaymentType.CREDIT && paidAmount > 0) {
            return "PARTIAL_CASH_AND_DEBT_REDUCTION";
        } else if (saleInvoice.getPaymentType() == PaymentType.CREDIT && paidAmount == 0) {
            return "DEBT_REDUCTION_ONLY";
        } else if (saleInvoice.getPaymentType() == PaymentType.CASH && saleInvoice.getRemainingAmount() > 0) {
            return "PARTIAL_CASH_REFUND";
        }
        
        return "UNKNOWN";
    }
    
    
    private float calculateCashRefund(SaleInvoice saleInvoice, float totalRefundAmount) {
        float paidAmount = saleInvoice.getPaidAmount();
        
        if (saleInvoice.getPaymentType() == PaymentType.CASH) {
            return Math.min(totalRefundAmount, paidAmount);
        } else if (saleInvoice.getPaymentType() == PaymentType.CREDIT && paidAmount > 0) {
            return Math.min(totalRefundAmount, paidAmount);
        }
        
        return 0.0f;
    }
    
    
    private float calculateDebtReduction(SaleInvoice saleInvoice, float totalRefundAmount) {
        float cashRefund = calculateCashRefund(saleInvoice, totalRefundAmount);
        return totalRefundAmount - cashRefund;
    }
}
