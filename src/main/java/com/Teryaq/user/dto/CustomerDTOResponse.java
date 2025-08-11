package com.Teryaq.user.dto;

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
@Schema(description = "Customer response with debt information")
public class CustomerDTOResponse {
    private Long id;
    
    @Schema(description = "Customer name", example = "cash customer")
    private String name;
    private String phoneNumber;
    private String address;
    
    @Schema(description = "Pharmacy ID", example = "1")
    private Long pharmacyId;
    
    private Float totalDebt; // إجمالي الديون
    private Float totalPaid; // إجمالي المدفوع
    private Float remainingDebt; // المتبقي من الديون
    private int activeDebtsCount; // عدد الديون النشطة
    private List<CustomerDebtDTOResponse> debts; // تفاصيل الديون
}
