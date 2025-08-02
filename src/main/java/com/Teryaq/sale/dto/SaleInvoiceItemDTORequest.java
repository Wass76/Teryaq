package com.Teryaq.sale.dto;

import com.Teryaq.product.Enum.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Sale Invoice Item Request", example = """
{
  "stockItemId": 1,
  "quantity": 2,
  "unitPrice": 800.0,
  "discountType": "FIXED_AMOUNT",
  "discountValue": 100.0
}
""")
public class SaleInvoiceItemDTORequest {
    @Schema(description = "Stock Item ID", example = "1")
    private Long stockItemId;
    
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;
    
    @Schema(description = "Unit Price (optional - will use stock price if not provided)", example = "800.0")
    private Float unitPrice; // سعر الوحدة (اختياري - إذا لم يتم تحديده سيتم أخذه من المخزون)
    
    // الخصم على هذا العنصر
    @Schema(description = "Discount Type", example = "FIXED_AMOUNT", allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"})
    private DiscountType discountType;
    
    @Schema(description = "Discount Value", example = "100.0")
    private Float discountValue;
    
    // للتوافق مع الـ JSON القديم
    @Schema(description = "Discount (Legacy field)", example = "100.0")
    private Float discount;
    
    // للتوافق مع الكود القديم
    public Float getDiscountValue() {
        if (discount != null) {
            return discount;
        }
        return discountValue != null ? discountValue : 0f;
    }
    
    public DiscountType getDiscountType() {
        return discountType != null ? discountType : DiscountType.FIXED_AMOUNT;
    }
} 