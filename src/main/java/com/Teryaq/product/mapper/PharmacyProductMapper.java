package com.Teryaq.product.mapper;

import com.Teryaq.product.repo.CategoryRepo;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.PharmacyProductDTORequest;
import com.Teryaq.product.dto.PharmacyProductDTOResponse;
import com.Teryaq.product.dto.PharmacyProductListDTO;
import com.Teryaq.product.dto.ProductMultiLangDTOResponse;
//import com.Teryaq.product.dto.PharmacyProductTranslationDTOResponse;
import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.PharmacyProductBarcode;
import com.Teryaq.product.entity.PharmacyProductTranslation;
import com.Teryaq.product.repo.TypeRepo;
import com.Teryaq.product.repo.FormRepo;
import com.Teryaq.product.repo.ManufacturerRepo;
import com.Teryaq.user.repository.PharmacyRepository;

import jakarta.persistence.EntityNotFoundException;
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

    public PharmacyProduct toEntity(PharmacyProductDTORequest dto, Long pharmacyId) {
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

        if (pharmacyId != null) {
            product.setPharmacy(pharmacyRepository.findById(pharmacyId)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with ID: " + pharmacyId)));
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

    public PharmacyProductDTOResponse toResponse(PharmacyProduct product, String lang) {
        String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
        
        System.out.println("🔍 DEBUG: toResponse called with lang: " + sanitizedlang);
        System.out.println("🔍 DEBUG: Product has " + (product.getTranslations() != null ? product.getTranslations().size() : 0) + " translations");
        
        if (product.getTranslations() != null) {
            product.getTranslations().forEach(t -> 
                System.out.println("🔍 DEBUG: Available translation - lang: " + t.getLanguage().getCode() + ", tradeName: " + t.getTradeName())
            );
        }
        
        PharmacyProductTranslation translation = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
                .findFirst()
                .orElse(null)
                : null;

        // Set<PharmacyProductTranslationDTOResponse> allTranslations = product.getTranslations() != null 
        //     ? product.getTranslations().stream()
        //         .map(translationMapper::toResponse)
        //         .collect(Collectors.toSet())
        //     : new HashSet<>();


        

        
        System.out.println("🔍 DEBUG: Building response - Original tradeName: " + product.getTradeName() + ", scientificName: " + product.getScientificName());
        if (translation != null) {
            System.out.println("🔍 DEBUG: Translation found - tradeName: " + translation.getTradeName() + ", scientificName: " + translation.getScientificName());
            System.out.println("🔍 DEBUG: Translation language: " + translation.getLanguage().getCode());
        } else {
            System.out.println("⚠️ DEBUG: No translation found for lang: " + sanitizedlang);
        }
        
        String finalTradeName = translation != null ? translation.getTradeName() : product.getTradeName();
        String finalScientificName = translation != null ? translation.getScientificName() : product.getScientificName();
        
        System.out.println("🔍 DEBUG: Final response values - tradeName: " + finalTradeName + ", scientificName: " + finalScientificName);
        
        return PharmacyProductDTOResponse.builder()
                .id(product.getId())
                .tradeName(finalTradeName)
                .scientificName(finalScientificName)
                .concentration(product.getConcentration())
                .size(product.getSize())
                .refPurchasePrice(product.getRefPurchasePrice())
                .refSellingPrice(product.getRefSellingPrice())
                .notes(product.getNotes())  
                .tax(product.getTax())
                .barcodes(product.getBarcodes() != null ? 
                    product.getBarcodes().stream()
                        .map(PharmacyProductBarcode::getBarcode)
                        .collect(Collectors.toSet()) : new HashSet<>())
                // .productType(ProductType.PHARMACY)
                .productTypeName(ProductType.PHARMACY.getTranslatedName(sanitizedlang))
                .requiresPrescription(product.getRequiresPrescription())
               

                 
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .categories(
                        product.getCategories() != null ? product.getCategories().stream()
                                .map(category -> {
                                    if (category.getTranslations() == null) return category.getName();
                                    return category.getTranslations().stream()
                                            .filter(t -> sanitizedlang.equalsIgnoreCase(t.getLanguage().getCode()))
                                            .findFirst()
                                            .map(com.Teryaq.product.entity.CategoryTranslation::getName)
                                            .orElse(category.getName());
                                })
                                .collect(Collectors.toSet()) : new HashSet<>()
                )

                .type(
                        product.getType() != null
                                ? product.getType().getTranslations().stream()
                                .filter(t -> sanitizedlang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.TypeTranslation::getName)
                                .orElse(product.getType().getName())
                                : null
                )

                .form(
                        product.getForm() != null
                                ? product.getForm().getTranslations().stream()
                                .filter(t -> sanitizedlang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.FormTranslation::getName)
                                .orElse(product.getForm().getName())
                                : null
                )

                .manufacturer(
                        product.getManufacturer() != null
                                ? product.getManufacturer().getTranslations().stream()
                                .filter(t -> sanitizedlang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                                .orElse(product.getManufacturer().getName())
                                : null
                )
              //  .translations(allTranslations)

                .build();
    }

    public void updateEntityFromRequest(PharmacyProduct existing, PharmacyProductDTORequest dto, Long pharmacyId) {
        System.out.println("🔍 DEBUG: updateEntityFromRequest - Request tradeName: " + dto.getTradeName() + ", scientificName: " + dto.getScientificName());
        
        if (dto.getTradeName() != null) {
            existing.setTradeName(dto.getTradeName());
            System.out.println("✅ DEBUG: Updated tradeName to: " + existing.getTradeName());
        }
        if (dto.getScientificName() != null) {
            existing.setScientificName(dto.getScientificName());
            System.out.println("✅ DEBUG: Updated scientificName to: " + existing.getScientificName());
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
            // تحقق من الباركودات الموجودة حالياً
            Set<String> existingBarcodes = existing.getBarcodes().stream()
                    .map(PharmacyProductBarcode::getBarcode)
                    .collect(Collectors.toSet());
            
            Set<String> newBarcodes = new HashSet<>(dto.getBarcodes());
            
            // إزالة الباركودات التي لم تعد موجودة في الطلب
            existing.getBarcodes().removeIf(barcode -> !newBarcodes.contains(barcode.getBarcode()));
            
            // إضافة الباركودات الجديدة فقط
            Set<String> barcodesToAdd = newBarcodes.stream()
                    .filter(barcode -> !existingBarcodes.contains(barcode))
                    .collect(Collectors.toSet());
            
            for (String barcodeStr : barcodesToAdd) {
                PharmacyProductBarcode barcode = new PharmacyProductBarcode();
                barcode.setBarcode(barcodeStr);
                barcode.setProduct(existing);
                existing.getBarcodes().add(barcode);
            }
        }
        if (dto.getRequiresPrescription() != null) {
            existing.setRequiresPrescription(dto.getRequiresPrescription());
        }

        // تحديث الصيدلية
        if (pharmacyId != null) {
            existing.setPharmacy(pharmacyRepository.findById(pharmacyId)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found with ID: " + pharmacyId)));
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

    public PharmacyProductListDTO toListDTO(PharmacyProduct product, String lang) {
        String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
        PharmacyProductTranslation translation = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
                .findFirst()
                .orElse(null)
                : null;
        return PharmacyProductListDTO.builder()
                .id(product.getId())
                .tradeName(translation != null ? translation.getTradeName() : product.getTradeName())
                .scientificName(translation != null ? translation.getScientificName() : product.getScientificName())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .requiresPrescription(product.getRequiresPrescription())
                .barcodes(product.getBarcodes() != null ? 
                    product.getBarcodes().stream()
                        .map(PharmacyProductBarcode::getBarcode)
                        .collect(Collectors.toSet()) : new HashSet<>())
                //.productType(ProductType.PHARMACY)
                .productTypeName(ProductType.PHARMACY.getTranslatedName(sanitizedlang))
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .build();
    }

    public ProductMultiLangDTOResponse toMultiLangResponse(PharmacyProduct product) {
        if (product == null) return null;

        // استخراج الاسم التجاري باللغتين
        String tradeNameAr = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(PharmacyProductTranslation::getTradeName)
                .findFirst()
                .orElse(product.getTradeName());

        String tradeNameEn = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(PharmacyProductTranslation::getTradeName)
                .findFirst()
                .orElse(product.getTradeName());

        // استخراج الاسم العلمي باللغتين
        String scientificNameAr = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(PharmacyProductTranslation::getScientificName)
                .findFirst()
                .orElse(product.getScientificName());

        String scientificNameEn = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(PharmacyProductTranslation::getScientificName)
                .findFirst()
                .orElse(product.getScientificName());

        return ProductMultiLangDTOResponse.builder()
                .id(product.getId())
                .tradeNameAr(tradeNameAr)
                .tradeNameEn(tradeNameEn)
                .scientificNameAr(scientificNameAr)
                .scientificNameEn(scientificNameEn)
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .build();
    }



}
