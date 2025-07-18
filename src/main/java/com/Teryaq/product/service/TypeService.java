package com.Teryaq.product.service;

import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.dto.TypeDTOResponse;
import com.Teryaq.product.entity.TypeTranslation;
import com.Teryaq.product.entity.Type;
import com.Teryaq.product.mapper.TypeMapper;
import com.Teryaq.product.repo.TypeRepo;
import com.Teryaq.product.repo.TypeTranslationRepo;
import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class TypeService {

    private final TypeRepo typeRepo;
    private final TypeMapper typeMapper;
    private final LanguageRepo languageRepo;
    private final TypeTranslationRepo typeTranslationRepo;

    public TypeService(TypeRepo typeRepo, TypeMapper typeMapper, LanguageRepo languageRepo, TypeTranslationRepo typeTranslationRepo) {
        this.typeRepo = typeRepo;
        this.typeMapper = typeMapper;
        this.languageRepo = languageRepo;
        this.typeTranslationRepo = typeTranslationRepo;
    }

    public List<TypeDTOResponse> getTypes(String langCode) {
        log.info("Getting types with langCode: {}", langCode);
        
        List<Type> types = typeRepo.findAllWithTranslations();
        log.info("Found {} types", types.size());
    
        return types.stream()
                .map(type -> {
                    log.info("Processing type: {} with {} translations", type.getName(),
                            type.getTranslations() != null ? type.getTranslations().size() : 0);
    
                    return typeMapper.toResponse(type, langCode);
                })
                .toList();
    }
    
    public TypeDTOResponse getByID(long id, String langCode) {
        Type type = typeRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
        return typeMapper.toResponse(type, langCode);
    }

    public TypeDTOResponse insertType(TypeDTORequest dto, String langCode) {
    
            if (typeRepo.existsByName(dto.getName())) {
                throw new ConflictException("Type with name '" + dto.getName() + "' already exists");
            }
    
            Type type = new Type();
            type.setName(dto.getName());
            Type savedType = typeRepo.save(type);
    
            List<TypeTranslation> translations = dto.getTranslations().stream()
                .map(t -> {
                    Language lang = languageRepo.findByCode(t.getLanguageCode())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                    return new TypeTranslation(t.getName(), savedType, lang);
                })
                .collect(Collectors.toList());
    
            typeTranslationRepo.saveAll(translations);
    
            savedType.setTranslations(new HashSet<>(translations));
    
            return typeMapper.toResponse(savedType, langCode);
            
    }

    
    
    public TypeDTOResponse editType(Long id, TypeDTORequest dto, String langCode) {
    return typeRepo.findByIdWithTranslations(id).map(existing -> {
        if (!existing.getName().equals(dto.getName()) && typeRepo.existsByName(dto.getName())) {
            throw new ConflictException("Type with name '" + dto.getName() + "' already exists");
        }

        existing.setName(dto.getName());
        Type saved = typeRepo.save(existing);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            typeTranslationRepo.deleteByType(saved);

            List<TypeTranslation> translations = dto.getTranslations().stream()
                    .map(t -> {
                        Language lang = languageRepo.findByCode(t.getLanguageCode())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                        return new TypeTranslation(t.getName(), saved, lang);
                    })
                    .toList();

            typeTranslationRepo.saveAll(translations);
        }

        // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
        Type updated = typeRepo.findByIdWithTranslations(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Updated type not found"));

        return typeMapper.toResponse(updated, langCode);

    }).orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
}

    

    public void deleteType(Long id) {
        if (!typeRepo.existsById(id)) {
            throw new EntityNotFoundException("Type with ID " + id + " not found");
        }
        typeRepo.deleteById(id);
    }
}
