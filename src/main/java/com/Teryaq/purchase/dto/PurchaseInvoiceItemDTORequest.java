package com.Teryaq.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import com.Teryaq.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@Schema(description = "Purchase Invoice Item Request", example = """
{
  "productId": 1,
  "receivedQty": 100,
  "bonusQty": 10,
  "invoicePrice": 5.50,
  "batchNo": "BATCH001",
  "invoiceNumber": "INV-2024-001",
  "expiryDate": "2025-12-31",
  "productType": "MASTER"
}
""")
public class PurchaseInvoiceItemDTORequest {
    
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @Schema(description = "Quantity received", example = "100")
    private Integer receivedQty;
    
    @Schema(description = "Bonus quantity", example = "10")
    private Integer bonusQty;
    
    @Schema(description = "Invoice price per unit", example = "5.50")
    private Double invoicePrice;
    
    @Schema(description = "Batch number", example = "BATCH001")
    private String batchNo;

    @Schema(description = "Invoice number from supplier", example = "INV-2024-001")
    private String invoiceNumber;

    @Schema(description = "Expiry date", example = "2025-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Schema(description = "Product type", example = "MASTER", 
            allowableValues = {"MASTER", "PHARMACY"})
    private ProductType productType; // 'MASTER' or 'PHARMACY'
} 