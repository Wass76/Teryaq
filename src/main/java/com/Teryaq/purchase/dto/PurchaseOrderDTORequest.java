package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Purchase Order Request", example = """
{
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 5.50
    }
  ]
}
""")
public class PurchaseOrderDTORequest {
    
    @Schema(description = "Supplier ID", example = "1")
    private Long supplierId;
    
    @Schema(description = "Currency for the purchase order", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED"})
    private Currency currency;
    
    @Schema(description = "List of items to purchase")
    private List<PurchaseOrderItemDTORequest> items;
} 