package com.Teryaq.product.service;

import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.MProductDTOResponse;
import com.Teryaq.product.dto.SearchDTORequest;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.mapper.MasterProductMapper;
import com.Teryaq.product.repo.MasterProductRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MasterProductService {

    private final MasterProductRepo masterProductRepo;
    private final MasterProductMapper masterProductMapper;
    private final LanguageRepo languageRepo;


    public List<MProductDTOResponse> getMasterProduct(String langCode) {
        return masterProductRepo.findAll().stream()
                .map(product -> masterProductMapper.toResponse(product, langCode))
                .toList();
    }

    public MProductDTOResponse getByID(long id, String langCode) {
        MasterProduct product = masterProductRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
        return masterProductMapper.toResponse(product, langCode);
    }


    public List<MProductDTOResponse> search(SearchDTORequest requestDTO) {
        List<MasterProduct> products = masterProductRepo.search(
                requestDTO.getKeyword(),
                requestDTO.getLanguageCode()
        );

        return products.stream()
                .map(product -> masterProductMapper
                        .toResponse(product, requestDTO.getLanguageCode()))
                .toList();
    }

    public MProductDTOResponse insertMasterProduct(MProductDTORequest requestDTO, String langCode) {
        MasterProduct product = masterProductMapper.toEntity(requestDTO);
        product.setDataSource("master");
        MasterProduct saved = masterProductRepo.save(product);
        return masterProductMapper.toResponse(saved, langCode);
    }


    public MProductDTOResponse editMasterProduct(Long id, MProductDTORequest requestDTO, String langCode) {
        return masterProductRepo.findById(id).map(existing -> {
            MasterProduct updated = masterProductMapper.toEntity(requestDTO);
            updated.setId(existing.getId());
            updated.setDataSource(existing.getDataSource());
            MasterProduct saved = masterProductRepo.save(updated);
            return masterProductMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
    }

    public void deleteMasterProduct(Long id) {
        if(!masterProductRepo.existsById(id)) {
            throw new EntityNotFoundException("Master Product with ID " + id + " not found!") ;
        }
        masterProductRepo.deleteById(id);}


    private Long getLanguageIdByCode(String langCode) {
        return languageRepo.findByCode(langCode)
                .orElseThrow(() -> new EntityNotFoundException("Language with code " + langCode + " not found"))
                .getId();
    }

}
