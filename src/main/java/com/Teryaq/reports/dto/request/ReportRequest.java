package com.Teryaq.reports.dto.request;

import com.Teryaq.reports.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request DTO for SRS report generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private String pharmacyId; // Optional for admin reports
    
    private String currency = "SYP"; // Default to Syrian Pound
    
    private String groupBy = "day"; // day, week, month, year
    
    private boolean includeDetails = true;
    
    private boolean includeCharts = true;
    
    private String format = "json"; // json, pdf, excel, csv
    
    private Map<String, Object> filters; // Additional filters
    
    private String language = "ar"; // Default to Arabic
}
