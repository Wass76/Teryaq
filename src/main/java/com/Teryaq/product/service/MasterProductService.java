package com.Teryaq.product.service;

import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.MProductDTOResponse;
import com.Teryaq.product.dto.SearchDTORequest;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.mapper.MasterProductMapper;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class MasterProductService {

    private final MasterProductRepo masterProductRepo;
    private final MasterProductMapper masterProductMapper;


    public Page<MProductDTOResponse> getMasterProduct(String langCode , Pageable pageable) {
        return masterProductRepo.findAll(pageable).map(
                product -> masterProductMapper.toResponse(product, langCode)
        );
    }

    public MProductDTOResponse getByID(long id, String langCode) {
        MasterProduct product = masterProductRepo.findById(id)
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
        product.setDataSource("master");
        MasterProduct saved = masterProductRepo.save(product);
        return masterProductMapper.toResponse(saved, langCode);
    }


    public MProductDTOResponse editMasterProduct(Long id, MProductDTORequest requestDTO, String langCode) {
        return masterProductRepo.findById(id).map(existing -> {
            MasterProduct updated = masterProductMapper.updateRequestToEntity(requestDTO);
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



}
