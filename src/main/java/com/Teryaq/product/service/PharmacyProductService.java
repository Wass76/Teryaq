package com.Teryaq.product.service;

import com.Teryaq.product.dto.PharmacyProductDTORequest;
import com.Teryaq.product.dto.PharmacyProductDTOResponse;
import com.Teryaq.product.dto.PharmacyProductListDTO;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.PharmacyProductTranslation;
import com.Teryaq.product.mapper.PharmacyProductMapper;
import com.Teryaq.product.repo.PharmacyProductBarcodeRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.PharmacyProductTranslationRepo;
import com.Teryaq.product.repo.MasterProductRepo;
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
    private final MasterProductRepo masterProductRepo;

    public Page<PharmacyProductListDTO> getPharmacyProduct(String langCode , Pageable pageable) {
        return pharmacyProductRepo.findAll(pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, langCode)
        );
    }

    public Page<PharmacyProductListDTO> getPharmacyProductByPharmacyId(Long pharmacyId, String langCode, Pageable pageable) {
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, langCode)
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

    public PharmacyProductDTOResponse insertPharmacyProduct(PharmacyProductDTORequest requestDTO, 
                                                            String langCode, 
                                                            Long pharmacyId) {
        // تحقق من الباركودات إذا كانت موجودة مسبقاً
        if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
            for (String barcode : requestDTO.getBarcodes()) {
                if (pharmacyProductRepo.existsByBarcode(barcode)) {
                    throw new ConflictException("Barcode " + barcode + " already exists");
                }
                // تحقق من عدم وجود الباركود في الماستر
                if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                    throw new ConflictException("Barcode " + barcode + " already exists in master products");
                }
            }
        }
        
        // تحقق من وجود الصيدلية
        if (pharmacyId == null) {
            throw new ConflictException("Pharmacy ID is required");
        }
        
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new EntityNotFoundException("Pharmacy with ID " + pharmacyId + " not found");
        }
        
        PharmacyProduct product = pharmacyProductMapper.toEntity(requestDTO, pharmacyId);
        PharmacyProduct saved = pharmacyProductRepo.save(product);

        // حفظ الترجمات
        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            List<PharmacyProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language lang = languageRepo.findByCode(t.getLanguageCode())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                    return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(), saved, lang);
                })
                .collect(Collectors.toList());

            pharmacyProductTranslationRepo.saveAll(translations);
            saved.setTranslations(new HashSet<>(translations));
        }

        return pharmacyProductMapper.toResponse(saved, langCode);
    }

    public PharmacyProductDTOResponse editPharmacyProduct(Long id,
                                                         PharmacyProductDTORequest requestDTO,
                                                         String langCode,
                                                         Long pharmacyId) {
        if (pharmacyId == null) {
            throw new ConflictException("Pharmacy ID is required");
        }

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
                    // تحقق من عدم وجود الباركود في الماستر
                    if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                        throw new ConflictException("Barcode " + barcode + " already exists in master products");
                    }
                }
            }
            
            // تحقق من وجود الصيدلية إذا تم تحديثها
            if (pharmacyId != null && !pharmacyRepository.existsById(pharmacyId)) {
                throw new EntityNotFoundException("Pharmacy with ID " + pharmacyId + " not found");
            }
            
            // استخدام دالة المابير لتحديث الكائن
            pharmacyProductMapper.updateEntityFromRequest(existing, requestDTO, pharmacyId);
            
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
                        return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(),  saved, lang);
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


