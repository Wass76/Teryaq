package com.Teryaq.product.entity;

public enum ProductType {
    MASTER("Master", "مركزي"),
    PHARMACY("Pharmacy", "صيدلية");
    
    private final String englishName;
    private final String arabicName;
    
    ProductType(String englishName, String arabicName) {
        this.englishName = englishName;
        this.arabicName = arabicName;
    }
    
    public String getTranslatedName(String languageCode) {
        return "ar".equalsIgnoreCase(languageCode) ? arabicName : englishName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
} 