package com.Teryaq.product.Enum;

public enum DiscountType {
    PERCENTAGE("نسبة مئوية"),
    FIXED_AMOUNT("مبلغ ثابت");
    
    private final String arabicName;
    
    DiscountType(String arabicName) {
        this.arabicName = arabicName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
    
    public String getTranslatedName(String languageCode) {
        return "ar".equalsIgnoreCase(languageCode) ? arabicName : this.name();
    }
} 