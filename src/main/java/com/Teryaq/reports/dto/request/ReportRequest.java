package com.Teryaq.reports.dto.request;

import com.Teryaq.reports.enums.Currency;
import com.Teryaq.reports.enums.Language;
import com.Teryaq.reports.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Simplified Report Request DTO
 * Used for generating reports with the required parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    // For daily reports, use this instead of startDate/endDate
    private LocalDate date;
    
    @Builder.Default
    private Currency currency = Currency.SYP;
    
    @Builder.Default
    private Language language = Language.EN;
}
