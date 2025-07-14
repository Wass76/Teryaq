package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductDTORequest;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductDTOResponse;
import com.Teryaq.product.aPharmacyProduct.dto.PharmacyProductListDTO;
import com.Teryaq.user.repository.PharmacyRepository;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyProductService {

    private final PharmacyProductRepo pharmacyProductRepo;
    private final PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo;
    private final PharmacyProductMapper pharmacyProductMapper;
    private final PharmacyRepository pharmacyRepository;
    private final LanguageRepo languageRepo;
    private final PharmacyProductTranslationRepo pharmacyProductTranslationRepo;

    public Page<PharmacyProductListDTO> getPharmacyProduct(String langCode , Pageable pageable) {
        return pharmacyProductRepo.findAll(pageable).map(
                product -> pharmacyProductMapper.toListDTO(product)
        );
    }

    public Page<PharmacyProductListDTO> getPharmacyProductByPharmacyId(Long pharmacyId, String langCode, Pageable pageable) {
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product)
        );
    }

    public Page<PharmacyProductDTOResponse> getPharmacyProductByPharmacyIdWithTranslation(Long pharmacyId, String langCode, Pageable pageable) {
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toResponse(product, langCode)
        );
    }

    public Page<PharmacyProduct> getPharmacyProduct(Pageable pageable) {
        return pharmacyProductRepo.findAll(pageable);
    }

    public PharmacyProductDTOResponse getByID(long id, String langCode) {
        PharmacyProduct product = pharmacyProductRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toResponse(product, langCode);
    }

    public PharmacyProductDTOResponse insertPharmacyProduct(PharmacyProductDTORequest requestDTO, String langCode) {
        // تحقق من الباركودات إذا كانت موجودة مسبقاً
        if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
            for (String barcode : requestDTO.getBarcodes()) {
                if (pharmacyProductRepo.existsByBarcode(barcode)) {
                    throw new ConflictException("Barcode " + barcode + " already exists");
                }
            }
        }
        
        // تحقق من وجود الصيدلية
        if (requestDTO.getPharmacyId() == null) {
            throw new ConflictException("Pharmacy ID is required");
        }
        
        if (!pharmacyRepository.existsById(requestDTO.getPharmacyId())) {
            throw new EntityNotFoundException("Pharmacy with ID " + requestDTO.getPharmacyId() + " not found");
        }
        
        PharmacyProduct product = pharmacyProductMapper.toEntity(requestDTO);
        PharmacyProduct saved = pharmacyProductRepo.save(product);

        // حفظ الترجمات
        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            List<PharmacyProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language lang = languageRepo.findByCode(t.getLanguageCode())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                    return new PharmacyProductTranslation(null, t.getTradeName(), t.getScientificName(), t.getNotes(), saved, lang);
                })
                .collect(Collectors.toList());

            pharmacyProductTranslationRepo.saveAll(translations);
            saved.setTranslations(new HashSet<>(translations));
        }

        return pharmacyProductMapper.toResponse(saved, langCode);
    }

    public PharmacyProductDTOResponse editPharmacyProduct(Long id, PharmacyProductDTORequest requestDTO, String langCode) {
        return pharmacyProductRepo.findByIdWithTranslations(id).map(existing -> {
            // تحقق من تكرار الباركودات إذا تم تغييرها
            if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
                for (String barcode : requestDTO.getBarcodes()) {
                    // تحقق من وجود الباركود في منتجات أخرى
                    if (pharmacyProductBarcodeRepo.existsByBarcode(barcode)) {
                        // تحقق من أن الباركود لا ينتمي للمنتج الحالي
                        boolean belongsToCurrentProduct = existing.getBarcodes().stream()
                                .anyMatch(b -> b.getBarcode().equals(barcode));
                        if (!belongsToCurrentProduct) {
                            throw new ConflictException("Barcode " + barcode + " already exists");
                        }
                    }
                }
            }
            
            // تحقق من وجود الصيدلية إذا تم تحديثها
            if (requestDTO.getPharmacyId() != null && !pharmacyRepository.existsById(requestDTO.getPharmacyId())) {
                throw new EntityNotFoundException("Pharmacy with ID " + requestDTO.getPharmacyId() + " not found");
            }
            
            // استخدام دالة المابير لتحديث الكائن
            pharmacyProductMapper.updateEntityFromRequest(existing, requestDTO);
            
            PharmacyProduct saved = pharmacyProductRepo.save(existing);

            // تحديث الترجمات
            if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
                // حذف الترجمات القديمة
                pharmacyProductTranslationRepo.deleteByProduct(saved);

                // إنشاء الترجمات الجديدة
                List<PharmacyProductTranslation> translations = requestDTO.getTranslations().stream()
                    .map(t -> {
                        Language lang = languageRepo.findByCode(t.getLanguageCode())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                        return new PharmacyProductTranslation(null, t.getTradeName(), t.getScientificName(), t.getNotes(), saved, lang);
                    })
                    .collect(Collectors.toList());

                pharmacyProductTranslationRepo.saveAll(translations);
                saved.setTranslations(new HashSet<>(translations));
            }

            return pharmacyProductMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
    }

    public void deletePharmacyProduct(Long id) {
        if(!pharmacyProductRepo.existsById(id)) {
            throw new EntityNotFoundException("Pharmacy Product with ID " + id + " not found!") ;
        }
        pharmacyProductRepo.deleteById(id);
    }
}
