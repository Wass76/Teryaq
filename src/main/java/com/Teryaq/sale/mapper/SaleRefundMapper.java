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

@Component
public class SaleRefundMapper {
    
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
        return SaleRefundDTOResponse.builder()
                .refundId(refund.getId())
                .saleInvoiceId(refund.getSaleInvoice().getId())
                .totalRefundAmount(refund.getTotalRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundDate(refund.getRefundDate())
                .stockRestored(refund.getStockRestored())
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
        // يمكن تحسين هذا الجزء حسب بنية المنتج
        if (item.getStockItem() != null) {
            return "Product ID: " + item.getStockItem().getProductId();
        }
        return "Unknown Product";
    }
}
