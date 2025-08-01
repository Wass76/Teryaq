package com.Teryaq.product.service;

import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.MProductDTOResponse;
import com.Teryaq.product.dto.SearchDTORequest;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.MasterProductTranslation;
import com.Teryaq.product.mapper.MasterProductMapper;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.product.repo.MasterProductTranslationRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.language.Language;

import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
public class MasterProductService {

    private final MasterProductRepo masterProductRepo;
    private final MasterProductMapper masterProductMapper;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductTranslationRepo masterProductTranslationRepo;
    private final LanguageRepo languageRepo;


    public Page<MProductDTOResponse> getMasterProduct(String langCode , Pageable pageable) {
        return masterProductRepo.findAll(pageable).map(
                product -> masterProductMapper.toResponse(product, langCode)
        );
    }

    public MProductDTOResponse getByID(long id, String langCode) {
        MasterProduct product = masterProductRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
        return masterProductMapper.toResponse(product, langCode);
    }


    public Page<MProductDTOResponse> search(SearchDTORequest requestDTO , Pageable pageable) {
        Page<MasterProduct> products = masterProductRepo.search(
                requestDTO.getKeyword(),
                requestDTO.getLanguageCode(),
                pageable
        );
        return products.map(product -> masterProductMapper.toResponse(product, requestDTO.getLanguageCode()));
    }

    public MProductDTOResponse insertMasterProduct(MProductDTORequest requestDTO, String langCode) {
        if(masterProductRepo.findByBarcode(requestDTO.getBarcode()).isPresent()) {
            throw new ConflictException("Barcode already exists");
        }
        MasterProduct product = masterProductMapper.toEntity(requestDTO);
        MasterProduct saved = masterProductRepo.save(product);

        // حفظ الترجمات إذا وجدت
        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            Set<MasterProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language lang = null;
                    if (t.getLanguageCode() != null) {
                        lang = languageRepo.findByCode(t.getLanguageCode())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                    } 
                    MasterProductTranslation translation = new MasterProductTranslation();
                    translation.setTradeName(t.getTradeName());
                    translation.setScientificName(t.getScientificName());
                    translation.setProduct(saved);
                    translation.setLanguage(lang);
                    return translation;
                })
                .collect(java.util.stream.Collectors.toSet());
            masterProductTranslationRepo.saveAll(translations);
            saved.setTranslations(translations);
        }

        return masterProductMapper.toResponse(saved, langCode);
    }


    public MProductDTOResponse editMasterProduct(Long id, MProductDTORequest requestDTO, String langCode) {
        return masterProductRepo.findByIdWithTranslations(id).map(existing -> {
            MasterProduct updated = masterProductMapper.updateRequestToEntity(requestDTO);
            updated.setId(existing.getId());
            MasterProduct saved = masterProductRepo.save(updated);
            return masterProductMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
    }

    public void deleteMasterProduct(Long id) {
        if(!masterProductRepo.existsById(id)) {
            throw new EntityNotFoundException("Master Product with ID " + id + " not found!") ;
        }
        
        // التحقق من عدم وجود منتجات صيدلية مرتبطة بنفس الباركود
        MasterProduct masterProduct = masterProductRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found!"));
        
        // التحقق من وجود منتجات صيدلية تستخدم نفس الباركود
        if (pharmacyProductRepo.existsByBarcode(masterProduct.getBarcode())) {
            throw new ConflictException("Cannot delete master product. There are pharmacy products using the same barcode: " + masterProduct.getBarcode());
        }
        
        masterProductRepo.deleteById(id);
    }



}
