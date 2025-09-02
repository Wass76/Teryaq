package com.Teryaq.reports.enums;

/**
 * Enum defining chart types for SRS reporting requirements
 */
public enum ChartType {
    
    PIE_CHART("pie", "Pie Chart", "رسم بياني دائري"),
    LINE_CHART("line", "Line Chart", "رسم بياني خطي"),
    BAR_CHART("bar", "Bar Chart", "رسم بياني عمودي"),
    AREA_CHART("area", "Area Chart", "رسم بياني مساحي"),
    TABLE("table", "Table", "جدول"),
    GAUGE("gauge", "Gauge", "مقياس");
    
    private final String code;
    private final String englishName;
    private final String arabicName;
    
    ChartType(String code, String englishName, String arabicName) {
        this.code = code;
        this.englishName = englishName;
        this.arabicName = arabicName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
    
    public String getName(String language) {
        return "ar".equals(language) ? arabicName : englishName;
    }
}
