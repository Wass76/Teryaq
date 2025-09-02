package com.Teryaq.reports.enums;

/**
 * Enum defining all report types based on SRS requirements
 */
public enum ReportType {
    
    // 3.5.1 Sales Reports (تقارير المبيعات)
    DAILY_SALES_SUMMARY("daily-sales-summary", "Daily Sales Summary", "تقارير المبيعات اليومية"),
    MONTHLY_SALES_SUMMARY("monthly-sales-summary", "Monthly Sales Summary", "تقارير المبيعات الشهرية"),
    
    // 3.5.2 Profit Reports (تقارير الأرباح)
    DAILY_PROFIT_SUMMARY("daily-profit-summary", "Daily Profit Summary", "تقارير الأرباح اليومية"),
    MONTHLY_PROFIT_SUMMARY("monthly-profit-summary", "Monthly Profit Summary", "تقارير الأرباح الشهرية"),
    
    // 3.5.3 Inventory Reports (تقارير المخزون)
    CURRENT_INVENTORY("current-inventory", "Current Inventory", "تقارير المخزون الحالي"),
    INVENTORY_MOVEMENT("inventory-movement", "Inventory Movement", "تقارير حركة المخزون"),
    
    // 3.5.4 Debt Reports (تقارير الديون)
    CUSTOMER_DEBT_SUMMARY("customer-debt-summary", "Customer Debt Summary", "تقارير ديون العملاء"),
    
    // 3.5.5 Purchase Reports (تقارير الشراء)
    DAILY_PURCHASE_SUMMARY("daily-purchase-summary", "Daily Purchase Summary", "تقارير الشراء اليومية"),
    MONTHLY_PURCHASE_SUMMARY("monthly-purchase-summary", "Monthly Purchase Summary", "تقارير الشراء الشهرية");
    
    private final String code;
    private final String englishName;
    private final String arabicName;
    
    ReportType(String code, String englishName, String arabicName) {
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
