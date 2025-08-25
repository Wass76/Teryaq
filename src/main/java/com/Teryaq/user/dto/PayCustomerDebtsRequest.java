package com.Teryaq.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.Teryaq.product.Enum.PaymentMethod;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Pay Customer Debts Request", example = """
{
  "totalPaymentAmount": 500.00,
  "paymentMethod": "CASH",
  "notes": "دفع عام للديون",
  "paymentStrategy": "OVERDUE_FIRST"
}
""")
public class PayCustomerDebtsRequest {
    
    @Schema(description = "Total payment amount to distribute across debts", example = "500.00")
    private BigDecimal totalPaymentAmount;
        
    @Schema(description = "Payment method", example = "CASH", allowableValues = {"CASH", "BANK_ACCOUNT"})
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Payment notes", example = "دفع عام للديون")
    private String notes;
    
    @Schema(description = "Payment distribution strategy", example = "OVERDUE_FIRST", 
            allowableValues = {"OVERDUE_FIRST", "FIFO", "LIFO", "HIGHEST_AMOUNT_FIRST"})
    private PaymentStrategy paymentStrategy;
    
    public enum PaymentStrategy {
        OVERDUE_FIRST,      // الديون المتأخرة أولاً
        FIFO,               // أول دين أولاً (First In, First Out)
        LIFO,               // آخر دين أولاً (Last In, First Out)
        HIGHEST_AMOUNT_FIRST // أعلى مبلغ أولاً
    }
}
