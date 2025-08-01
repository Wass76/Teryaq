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
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.PharmacyRepository;
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
    private final PharmacyRepository pharmacyRepository;
    private final LanguageRepo languageRepo;
    private final PharmacyProductTranslationRepo pharmacyProductTranslationRepo;
    private final MasterProductRepo masterProductRepo;

    public PharmacyProductService(PharmacyProductRepo pharmacyProductRepo,
                                PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo,
                                PharmacyProductMapper pharmacyProductMapper,
                                PharmacyRepository pharmacyRepository,
                                LanguageRepo languageRepo,
                                PharmacyProductTranslationRepo pharmacyProductTranslationRepo,
                                MasterProductRepo masterProductRepo,
                                UserRepository userRepository) {
        super(userRepository);
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.pharmacyProductBarcodeRepo = pharmacyProductBarcodeRepo;
        this.pharmacyProductMapper = pharmacyProductMapper;
        this.pharmacyRepository = pharmacyRepository;
        this.languageRepo = languageRepo;
        this.pharmacyProductTranslationRepo = pharmacyProductTranslationRepo;
        this.masterProductRepo = masterProductRepo;
    }

//    public Page<PharmacyProductListDTO> getPharmacyProduct(String langCode, Pageable pageable) {
//        Long currentPharmacyId = getCurrentUserPharmacyId();
//        return pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable).map(
//                product -> pharmacyProductMapper.toListDTO(product, langCode)
//        );
//    }

    public Page<PharmacyProductListDTO> getPharmacyProductByPharmacyId(Long pharmacyId, String langCode, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, langCode)
        );
    }

    public Page<PharmacyProductDTOResponse> getPharmacyProductByPharmacyIdWithTranslation(Long pharmacyId, String langCode, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toResponse(product, langCode)
        );
    }

    public Page<PharmacyProductListDTO> getPharmacyProduct(String langCode, Pageable pageable) {
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
                product -> pharmacyProductMapper.toListDTO(product, langCode)
        );
    }

    public PharmacyProductDTOResponse getByID(long id, String langCode) {
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
        return pharmacyProductMapper.toResponse(product, langCode);
    }

    public PharmacyProductDTOResponse insertPharmacyProduct(PharmacyProductDTORequest requestDTO, 
                                                            String langCode) {
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
        
        // تحقق من الباركودات إذا كانت موجودة مسبقاً
        if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
            for (String barcode : requestDTO.getBarcodes()) {
                if (pharmacyProductRepo.existsByBarcodeAndPharmacyId(barcode, currentPharmacyId)) {
                    throw new ConflictException("Barcode " + barcode + " already exists in this pharmacy");
                }
                // تحقق من عدم وجود الباركود في الماستر
                if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                    throw new ConflictException("Barcode " + barcode + " already exists in master products");
                }
            }
        }
        
        PharmacyProduct product = pharmacyProductMapper.toEntity(requestDTO, currentPharmacyId);
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
                                                         String langCode) {
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
            // تحقق من تكرار الباركودات إذا تم تغييرها
            if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
                for (String barcode : requestDTO.getBarcodes()) {
                    // تحقق من وجود الباركود في منتجات أخرى (غير المنتج الحالي) في نفس الصيدلية
                    boolean barcodeExistsInOtherProducts = pharmacyProductBarcodeRepo.existsByBarcodeAndProductIdNotAndPharmacyId(barcode, id, currentPharmacyId);
                    if (barcodeExistsInOtherProducts) {
                        throw new ConflictException("Barcode " + barcode + " already exists in another product in this pharmacy");
                    }
                    // تحقق من عدم وجود الباركود في الماستر
                    if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                        throw new ConflictException("Barcode " + barcode + " already exists in master products");
                    }
                }
            }
            
            // استخدام دالة المابير لتحديث الكائن
            pharmacyProductMapper.updateEntityFromRequest(existing, requestDTO, currentPharmacyId);
            
            // حفظ الكائن الرئيسي أولاً
            PharmacyProduct saved = pharmacyProductRepo.save(existing);
            
            // تحديث الترجمات بشكل منفصل لتجنب مشاكل Cascade
            if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
                // حذف الترجمات القديمة
                pharmacyProductTranslationRepo.deleteByProduct(saved);
                
                // إنشاء الترجمات الجديدة
                List<PharmacyProductTranslation> newTranslations = requestDTO.getTranslations().stream()
                    .map(t -> {
                        Language lang = languageRepo.findByCode(t.getLanguageCode())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                        return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(),  saved, lang);
                    })
                    .collect(Collectors.toList());

                pharmacyProductTranslationRepo.saveAll(newTranslations);
            }
            
            // إعادة تحميل الكائن مع الترجمات
            return pharmacyProductMapper.toResponse(pharmacyProductRepo.findByIdWithTranslations(saved.getId()).orElse(saved), langCode);
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


}


