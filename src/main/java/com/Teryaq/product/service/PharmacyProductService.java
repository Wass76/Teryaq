package com.Teryaq.product.service;

import com.Teryaq.product.dto.PharmacyProductDTORequest;

import com.Teryaq.product.dto.PharmacyProductDTOResponse;
import com.Teryaq.product.dto.PharmacyProductListDTO;
import com.Teryaq.product.dto.ProductMultiLangDTOResponse;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.PharmacyProductTranslation;
import com.Teryaq.product.mapper.PharmacyProductMapper;
import com.Teryaq.product.repo.PharmacyProductBarcodeRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.PharmacyProductTranslationRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.UnAuthorizedException;
import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class PharmacyProductService extends BaseSecurityService {

    private final PharmacyProductRepo pharmacyProductRepo;
    private final PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo;
    private final PharmacyProductMapper pharmacyProductMapper;
    private final LanguageRepo languageRepo;
    private final PharmacyProductTranslationRepo pharmacyProductTranslationRepo;
    private final MasterProductRepo masterProductRepo;

    public PharmacyProductService(PharmacyProductRepo pharmacyProductRepo,
                                PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo,
                                PharmacyProductMapper pharmacyProductMapper,
                                LanguageRepo languageRepo,
                                PharmacyProductTranslationRepo pharmacyProductTranslationRepo,
                                MasterProductRepo masterProductRepo,
                                UserRepository userRepository) {
        super(userRepository);
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.pharmacyProductBarcodeRepo = pharmacyProductBarcodeRepo;
        this.pharmacyProductMapper = pharmacyProductMapper;
        this.languageRepo = languageRepo;
        this.pharmacyProductTranslationRepo = pharmacyProductTranslationRepo;
        this.masterProductRepo = masterProductRepo;
    }

//    public Page<PharmacyProductListDTO> getPharmacyProduct(String lang, Pageable pageable) {
//        Long currentPharmacyId = getCurrentUserPharmacyId();
//        return pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable).map(
//                product -> pharmacyProductMapper.toListDTO(product, lang)
//        );
//    }

    public Page<PharmacyProductListDTO> getPharmacyProductByPharmacyId(Long pharmacyId, String lang, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, lang)
        );
    }

    public Page<PharmacyProductDTOResponse> getPharmacyProductByPharmacyIdWithTranslation(Long pharmacyId, String lang, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toResponse(product, lang)
        );
    }

    public Page<PharmacyProductListDTO> getPharmacyProduct(String lang, Pageable pageable) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, lang)
        );
    }

    public PharmacyProductDTOResponse getByID(long id, String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toResponse(product, lang);
    }

    public PharmacyProductDTOResponse insertPharmacyProduct(PharmacyProductDTORequest requestDTO, 
                                                            String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }

        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
            for (String barcode : requestDTO.getBarcodes()) {
                if (pharmacyProductRepo.existsByBarcodeAndPharmacyId(barcode, currentPharmacyId)) {
                    throw new ConflictException("Barcode " + barcode + " already exists in this pharmacy");
                }
                if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                    throw new ConflictException("Barcode " + barcode + " already exists in master products");
                }
            }
        }
        
        PharmacyProduct product = pharmacyProductMapper.toEntity(requestDTO, currentPharmacyId);
        PharmacyProduct saved = pharmacyProductRepo.save(product);

        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            List<PharmacyProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language language = languageRepo.findByCode(t.getLang())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                    return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(), saved, language);
                })
                .collect(Collectors.toList());

            pharmacyProductTranslationRepo.saveAll(translations);
            saved.setTranslations(new HashSet<>(translations));
        }

        return pharmacyProductMapper.toResponse(saved, lang);
    }

    public PharmacyProductDTOResponse editPharmacyProduct(Long id,
                                                         PharmacyProductDTORequest requestDTO,
                                                         String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();

        return pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId).map(existing -> {
            if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
                for (String barcode : requestDTO.getBarcodes()) {
                    boolean barcodeExistsInOtherProducts = pharmacyProductBarcodeRepo.existsByBarcodeAndProductIdNotAndPharmacyId(barcode, id, currentPharmacyId);
                    if (barcodeExistsInOtherProducts) {
                        throw new ConflictException("Barcode " + barcode + " already exists in another product in this pharmacy");
                    }
                    if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                        throw new ConflictException("Barcode " + barcode + " already exists in master products");
                    }
                }
            }
            
            pharmacyProductMapper.updateEntityFromRequest(existing, requestDTO, currentPharmacyId);
            
            PharmacyProduct saved = pharmacyProductRepo.save(existing);
            
            if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
                pharmacyProductTranslationRepo.deleteByProduct(saved);
                
                List<PharmacyProductTranslation> newTranslations = requestDTO.getTranslations().stream()
                    .map(t -> {
                        Language language = languageRepo.findByCode(t.getLang())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                        return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(),  saved, language);
                    })
                    .collect(Collectors.toList());

                pharmacyProductTranslationRepo.saveAll(newTranslations);
            }
            
            return pharmacyProductMapper.toResponse(pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(saved.getId(), currentPharmacyId).orElse(saved), lang);
        }).orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
    }

    public void deletePharmacyProduct(Long id) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        if(!pharmacyProductRepo.existsByIdAndPharmacyId(id, currentPharmacyId)) {
            throw new EntityNotFoundException("Pharmacy Product with ID " + id + " not found in this pharmacy!") ;
        }
        pharmacyProductRepo.deleteById(id);
    }

    public List<ProductMultiLangDTOResponse> getPharmacyProductsMultiLang() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllWithTranslations(currentPharmacyId);
        
        return pharmacyProducts.stream()
                .map(pharmacyProductMapper::toMultiLangResponse)
                .collect(Collectors.toList());
    }

    public ProductMultiLangDTOResponse getPharmacyProductByIdMultiLang(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toMultiLangResponse(product);
    }



}


