package com.Teryaq.product.service;

import com.Teryaq.product.aPharmacyProduct.PharmacyProductRepo;
import com.Teryaq.product.dto.UnifiedProductSearchDTO;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.aPharmacyProduct.PharmacyProduct;
import com.Teryaq.product.repo.MasterProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnifiedProductSearchService {

    private final MasterProductRepo masterProductRepo;
    private final PharmacyProductRepo pharmacyProductRepo;


    public List<UnifiedProductSearchDTO> searchProducts(String keyword, String languageCode) {
        List<UnifiedProductSearchDTO> results = new java.util.ArrayList<>();

        // البحث في منتجات الماستر باستخدام الـ repository المحسن
        Page<MasterProduct> masterProductsPage = masterProductRepo.search(keyword, languageCode, PageRequest.of(0, 1000));
        List<MasterProduct> masterProducts = masterProductsPage.getContent();

        // البحث في منتجات الصيدلية باستخدام الـ repository المحسن
        Page<PharmacyProduct> pharmacyProductsPage = pharmacyProductRepo.search(keyword, languageCode, PageRequest.of(0, 1000));
        List<PharmacyProduct> pharmacyProducts = pharmacyProductsPage.getContent();

        // تحويل منتجات الماستر
        results.addAll(masterProducts.stream()
                .map(product -> convertMasterProductToUnifiedDTO(product, languageCode))
                .collect(Collectors.toList()));

        // تحويل منتجات الصيدلية
        results.addAll(pharmacyProducts.stream()
                .map(product -> convertPharmacyProductToUnifiedDTO(product, languageCode))
                .collect(Collectors.toList()));

        return results;
    }

    public List<UnifiedProductSearchDTO> getAllProducts(String languageCode) {
        return searchProducts("", languageCode);
    }



    private UnifiedProductSearchDTO convertMasterProductToUnifiedDTO(MasterProduct product, String languageCode) {
        // الحصول على الترجمة
        String translatedTradeName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();

        String translatedScientificName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();

        String translatedNotes = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getNotes())
                .orElse(product.getNotes())
                : product.getNotes();

        return UnifiedProductSearchDTO.builder()
                .id(product.getId())
                .tradeName(translatedTradeName)
                .scientificName(translatedScientificName)
                .barcode(product.getBarcode())
                .productType("MASTER")
                .requiresPrescription(product.getRequiresPrescription())
                .concentration(product.getConcentration())
                .size(product.getSize())
                // .refPurchasePrice(product.getRefPurchasePrice())
                // .refSellingPrice(product.getRefSellingPrice())
                .notes(translatedNotes)
                .tax(product.getTax())
                .type(product.getType() != null
                        ? product.getType().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.TypeTranslation::getName)
                        .orElse(product.getType().getName())
                        : null)
                .form(product.getForm() != null
                        ? product.getForm().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.FormTranslation::getName)
                        .orElse(product.getForm().getName())
                        : null)
                .manufacturer(product.getManufacturer() != null
                        ? product.getManufacturer().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                        .orElse(product.getManufacturer().getName())
                        : null)
                .build();
    }

    private UnifiedProductSearchDTO convertPharmacyProductToUnifiedDTO(PharmacyProduct product, String languageCode) {
        // الحصول على الترجمة
        String translatedTradeName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();

        String translatedScientificName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();

        String translatedNotes = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(languageCode))
                .findFirst()
                .map(t -> t.getNotes())
                .orElse(product.getNotes())
                : product.getNotes();

        return UnifiedProductSearchDTO.builder()
                .id(product.getId())
                .tradeName(translatedTradeName)
                .scientificName(translatedScientificName)
                .barcode(product.getBarcodes() != null && !product.getBarcodes().isEmpty() 
                    ? product.getBarcodes().iterator().next().getBarcode() 
                    : null)
                .productType("PHARMACY")
                .requiresPrescription(product.getRequiresPrescription())
                .concentration(product.getConcentration())
                .size(product.getSize())
                // .refPurchasePrice(product.getRefPurchasePrice())
                // .refSellingPrice(product.getRefSellingPrice())
                .notes(translatedNotes)
                .tax(product.getTax())
                .type(product.getType() != null
                        ? product.getType().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.TypeTranslation::getName)
                        .orElse(product.getType().getName())
                        : null)
                .form(product.getForm() != null
                        ? product.getForm().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.FormTranslation::getName)
                        .orElse(product.getForm().getName())
                        : null)
                .manufacturer(product.getManufacturer() != null
                        ? product.getManufacturer().getTranslations().stream()
                        .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                        .orElse(product.getManufacturer().getName())
                        : null)
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .build();
    }
} 