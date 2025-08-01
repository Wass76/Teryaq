package com.Teryaq.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTOResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String address;
    private BigDecimal totalDebt; // إجمالي الديون
    private BigDecimal totalPaid; // إجمالي المدفوع
    private BigDecimal remainingDebt; // المتبقي من الديون
    private int activeDebtsCount; // عدد الديون النشطة
    private List<CustomerDebtDTOResponse> debts; // تفاصيل الديون
}
