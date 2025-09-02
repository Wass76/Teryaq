package com.Teryaq.reports.dto.response;

import com.Teryaq.reports.enums.ReportType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for SRS report generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private boolean success;
    
    private ReportData data;
    
    private ReportMetadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportData {
        private SummaryData summary;
        private List<DetailData> details;
        private Map<String, ChartData> charts;
        private FilterInfo filters;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryData {
        private Long totalRecords;
        private Double totalAmount;
        private String currency;
        private String period;
        private String reportName;
        private String reportNameAr;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailData {
        private String id;
        private String date;
        private Double amount;
        private String description;
        private String descriptionAr;
        private Map<String, Object> additionalData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String type;
        private String title;
        private String titleAr;
        private List<ChartPoint> data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPoint {
        private String label;
        private String labelAr;
        private Object value;
        private String color;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterInfo {
        private Map<String, Object> appliedFilters;
        private List<String> availableFilters;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportMetadata {
        private LocalDateTime generatedAt;
        private ReportType reportType;
        private String pharmacyId;
        private String generatedBy;
        private String version = "1.0";
        private String language;
    }
}
