package com.Teryaq.purchase.dto;

import com.Teryaq.user.Enum.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Purchase Invoice Request", example = """
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 5.50
    }
  ]
}
""")
public class PurchaseInvoiceDTORequest {
    
    @Schema(description = "Purchase order ID", example = "1")
    private Long purchaseOrderId;
    
    @Schema(description = "Supplier ID", example = "1")
    private Long supplierId;
    
    @Schema(description = "Currency for the invoice", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED"})
    private Currency currency;
    
    @Schema(description = "Total invoice amount", example = "550.00")
    private Double total;
    
    @Schema(description = "List of invoice items")
    private List<PurchaseInvoiceItemDTORequest> items;
} 