package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductDTORequest;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductDTOResponse;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductTranslationDTOResponse;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductListDTO;
import com.Teryaq.product.repo.CategoryRepo;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.repo.TypeRepo;
import com.Teryaq.product.repo.FormRepo;
import com.Teryaq.product.repo.ManufacturerRepo;
import com.Teryaq.user.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PharmacyProductMapper {

    private final CategoryRepo categoryRepo;
    private final TypeRepo typeRepo;
    private final FormRepo formRepo;
    private final ManufacturerRepo manufacturerRepo;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyProductTranslationMapper translationMapper;

    public PharmacyProduct toEntity(PharmacyProductDTORequest dto) {
        PharmacyProduct product = new PharmacyProduct();

        product.setTradeName(dto.getTradeName());
        product.setScientificName(dto.getScientificName());
        product.setConcentration(dto.getConcentration());
        product.setSize(dto.getSize());
        // product.setRefPurchasePrice(dto.getRefPurchasePrice());
        // product.setRefSellingPrice(dto.getRefSellingPrice());
        product.setNotes(dto.getNotes());
        product.setTax(dto.getTax());
        // إضافة الباركودات المتعددة
        if (dto.getBarcodes() != null && !dto.getBarcodes().isEmpty()) {
            Set<PharmacyProductBarcode> barcodes = dto.getBarcodes().stream()
                    .map(barcodeStr -> {
                        PharmacyProductBarcode barcode = new PharmacyProductBarcode();
                        barcode.setBarcode(barcodeStr);
                        barcode.setProduct(product);
                        return barcode;
                    })
                    .collect(Collectors.toSet());
            product.setBarcodes(barcodes);
        }
        product.setRequiresPrescription(dto.getRequiresPrescription());

        // إضافة الصيدلية
        if (dto.getPharmacyId() != null) {
            product.setPharmacy(pharmacyRepository.findById(dto.getPharmacyId())
                    .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + dto.getPharmacyId())));
        }

        if (dto.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepo.findAllById(dto.getCategoryIds())));
        }

        if (dto.getTypeId() != null)
            product.setType(typeRepo.findById(dto.getTypeId()).orElse(null));

        if (dto.getFormId() != null)
            product.setForm(formRepo.findById(dto.getFormId()).orElse(null));

        if (dto.getManufacturerId() != null)
            product.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));

        return product;
    }

    public PharmacyProductDTOResponse toResponse(PharmacyProduct product, String languageCode) {
        String sanitizedLangCode = languageCode == null ? "en" : languageCode.trim().toLowerCase();
        
        // الحصول على الترجمة المطلوبة
        PharmacyProductTranslation translation = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
                .findFirst()
                .orElse(null)
                : null;

        Set<PharmacyProductTranslationDTOResponse> allTranslations = product.getTranslations() != null 
            ? product.getTranslations().stream()
                .map(translationMapper::toResponse)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return PharmacyProductDTOResponse.builder()
                .id(product.getId())
                .tradeName(translation != null ? translation.getTradeName() : product.getTradeName())
                .scientificName(translation != null ? translation.getScientificName() : product.getScientificName())
                .concentration(product.getConcentration())
                .size(product.getSize())
                // .refPurchasePrice(product.getRefPurchasePrice())
                // .refSellingPrice(product.getRefSellingPrice())
                .notes(translation != null ? translation.getNotes() : product.getNotes())
                .tax(product.getTax())
                .barcodes(product.getBarcodes() != null ? 
                    product.getBarcodes().stream()
                        .map(PharmacyProductBarcode::getBarcode)
                        .collect(Collectors.toSet()) : new HashSet<>())
                .productType("PHARMACY")
                .requiresPrescription(product.getRequiresPrescription())
               

                 
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .categories(
                        product.getCategories() != null ? product.getCategories().stream()
                                .map(category -> {
                                    if (category.getTranslations() == null) return category.getName();
                                    return category.getTranslations().stream()
                                            .filter(t -> sanitizedLangCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                            .findFirst()
                                            .map(com.Teryaq.product.entity.CategoryTranslation::getName)
                                            .orElse(category.getName());
                                })
                                .collect(Collectors.toSet()) : new HashSet<>()
                )

                .type(
                        product.getType() != null
                                ? product.getType().getTranslations().stream()
                                .filter(t -> sanitizedLangCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.TypeTranslation::getName)
                                .orElse(product.getType().getName())
                                : null
                )

                .form(
                        product.getForm() != null
                                ? product.getForm().getTranslations().stream()
                                .filter(t -> sanitizedLangCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.FormTranslation::getName)
                                .orElse(product.getForm().getName())
                                : null
                )

                .manufacturer(
                        product.getManufacturer() != null
                                ? product.getManufacturer().getTranslations().stream()
                                .filter(t -> sanitizedLangCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                                .orElse(product.getManufacturer().getName())
                                : null
                )
                .translations(allTranslations)

                .build();
    }

    public void updateEntityFromRequest(PharmacyProduct existing, PharmacyProductDTORequest dto) {
        if (dto.getTradeName() != null) {
            existing.setTradeName(dto.getTradeName());
        }
        if (dto.getScientificName() != null) {
            existing.setScientificName(dto.getScientificName());
        }
        if (dto.getConcentration() != null) {
            existing.setConcentration(dto.getConcentration());
        }
        if (dto.getSize() != null) {
            existing.setSize(dto.getSize());
        }
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }
        if (dto.getTax() >= 0) {
            existing.setTax(dto.getTax());
        }
        // تحديث الباركودات المتعددة
        if (dto.getBarcodes() != null) {
            // حذف الباركودات الحالية
            existing.getBarcodes().clear();
            
            // إضافة الباركودات الجديدة
            Set<PharmacyProductBarcode> newBarcodes = dto.getBarcodes().stream()
                    .map(barcodeStr -> {
                        PharmacyProductBarcode barcode = new PharmacyProductBarcode();
                        barcode.setBarcode(barcodeStr);
                        barcode.setProduct(existing);
                        return barcode;
                    })
                    .collect(Collectors.toSet());
            existing.getBarcodes().addAll(newBarcodes);
        }
        if (dto.getRequiresPrescription() != null) {
            existing.setRequiresPrescription(dto.getRequiresPrescription());
        }

        // تحديث الصيدلية
        if (dto.getPharmacyId() != null) {
            existing.setPharmacy(pharmacyRepository.findById(dto.getPharmacyId())
                    .orElseThrow(() -> new RuntimeException("Pharmacy not found with ID: " + dto.getPharmacyId())));
        }

        // تحديث الكلاسات
        if (dto.getCategoryIds() != null) {
            // حذف الفئات الحالية وإضافة الجديدة بشكل آمن
            Set<Category> newCategories = new HashSet<>(categoryRepo.findAllById(dto.getCategoryIds()));
            existing.getCategories().clear();
            existing.getCategories().addAll(newCategories);
        }
        if (dto.getTypeId() != null) {
            existing.setType(typeRepo.findById(dto.getTypeId()).orElse(null));
        }
        if (dto.getFormId() != null) {
            existing.setForm(formRepo.findById(dto.getFormId()).orElse(null));
        }
        if (dto.getManufacturerId() != null) {
            existing.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));
        }
    }

    public PharmacyProductListDTO toListDTO(PharmacyProduct product) {
        return PharmacyProductListDTO.builder()
                .tradeName(product.getTradeName())
                .scientificName(product.getScientificName())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .requiresPrescription(product.getRequiresPrescription())
                .barcodes(product.getBarcodes() != null ? 
                    product.getBarcodes().stream()
                        .map(PharmacyProductBarcode::getBarcode)
                        .collect(Collectors.toSet()) : new HashSet<>())
                .productType("PHARMACY")
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .build();
    }
}
