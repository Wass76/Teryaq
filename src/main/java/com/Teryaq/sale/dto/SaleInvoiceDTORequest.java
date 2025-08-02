package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.Enum.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Sale Invoice Request", example = """
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 10.0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0,
      "discountType": "FIXED_AMOUNT",
      "discountValue": 100.0
    }
  ]
}
""")
public class SaleInvoiceDTORequest {
    @Schema(description = "Customer ID", example = "1")
    private Long customerId;        
    
    @Schema(description = "Payment Type", example = "CASH", allowableValues = {"CASH", "CREDIT"})
    private PaymentType paymentType;
    
    @Schema(description = "Payment Method", example = "CASH", allowableValues = {"CASH", "BANK_ACCOUNT"})
    private PaymentMethod paymentMethod;
    
    // الخصم على الفاتورة كاملة
    @Schema(description = "Invoice Discount Type", example = "PERCENTAGE", allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"})
    private DiscountType invoiceDiscountType;
    
    @Schema(description = "Invoice Discount Value", example = "10.0")
    private Float invoiceDiscountValue;
    
    // للتوافق مع الـ JSON القديم
    @Schema(description = "Discount (Legacy field)", example = "10.0")
    private Float discount;
    
    @Schema(description = "Paid Amount (null for auto-calculate)", example = "null")
    private Float paidAmount;
    
    @Schema(description = "Sale Items", example = "[]")
    private List<SaleInvoiceItemDTORequest> items;
    
    // للتوافق مع الكود القديم
    public Float getInvoiceDiscountValue() {
        if (discount != null) {
            return discount;
        }
        return invoiceDiscountValue != null ? invoiceDiscountValue : 0f;
    }
    
    public DiscountType getInvoiceDiscountType() {
        return invoiceDiscountType != null ? invoiceDiscountType : DiscountType.FIXED_AMOUNT;
    }
} 