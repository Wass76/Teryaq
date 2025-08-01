package com.Teryaq.product.Enum;

public enum PaymentMethod {
    CASH("نقدي", "Cash"),
    BANK_ACCOUNT("حساب البنك", "Bank Account");
    
    private final String arabicName;
    private final String englishName;
    
    PaymentMethod(String arabicName, String englishName) {
        this.arabicName = arabicName;
        this.englishName = englishName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getTranslatedName(String languageCode) {
        return "ar".equalsIgnoreCase(languageCode) ? arabicName : englishName;
    }
} 