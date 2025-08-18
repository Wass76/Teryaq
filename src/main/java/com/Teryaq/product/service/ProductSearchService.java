package com.Teryaq.product.service;

import com.Teryaq.product.dto.ProductSearchDTOResponse;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.mapper.ProductSearchMapper;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService extends BaseSecurityService {

    private final MasterProductRepo masterProductRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final ProductSearchMapper productSearchMapper;

    protected ProductSearchService(UserRepository userRepository, 
                                 MasterProductRepo masterProductRepo, 
                                 PharmacyProductRepo pharmacyProductRepo,
                                 ProductSearchMapper productSearchMapper) {
        super(userRepository);
        this.masterProductRepo = masterProductRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.productSearchMapper = productSearchMapper;
    }

    public Page<ProductSearchDTOResponse> searchProducts(String keyword, String lang, Pageable pageable) {
        List<ProductSearchDTOResponse> allResults = new ArrayList<>();

        Long currentPharmacyId = getCurrentUserPharmacyId();

        Page<MasterProduct> masterProductsPage = masterProductRepo.search(keyword, lang, PageRequest.of(0, 1000));
        List<MasterProduct> masterProducts = masterProductsPage.getContent();

        Page<PharmacyProduct> pharmacyProductsPage = pharmacyProductRepo.searchByPharmacyId(keyword, lang, currentPharmacyId, PageRequest.of(0, 1000));
        List<PharmacyProduct> pharmacyProducts = pharmacyProductsPage.getContent();

        allResults.addAll(masterProducts.stream()
                .map(product -> productSearchMapper.convertMasterProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList()));

        allResults.addAll(pharmacyProducts.stream()
                .map(product -> productSearchMapper.convertPharmacyProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList()));

        
        return applyPagination(allResults, pageable);
    }

    public Page<ProductSearchDTOResponse> getAllProducts(String lang, Pageable pageable) {
        return searchProducts("", lang, pageable);
    }

   
    private Page<ProductSearchDTOResponse> applyPagination(List<ProductSearchDTOResponse> allResults, Pageable pageable) {
        int totalElements = allResults.size();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);

        List<ProductSearchDTOResponse> pageContent = new ArrayList<>();
        if (startIndex < totalElements) {
            pageContent = allResults.subList(startIndex, endIndex);
        }

        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            totalElements
        );
    }
} 