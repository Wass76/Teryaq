package com.Teryaq.product.service;

import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.dto.TypeDTOResponse;
import com.Teryaq.product.entity.Type;
import com.Teryaq.product.entity.TypeTranslation;
import com.Teryaq.product.mapper.TypeMapper;
import com.Teryaq.product.repo.TypeRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeService {

    private final TypeRepo typeRepo;
    private final TypeMapper typeMapper;
    private final LanguageRepo languageRepo;

    public TypeService(TypeRepo typeRepo, TypeMapper typeMapper, LanguageRepo languageRepo) {
        this.typeRepo = typeRepo;
        this.typeMapper = typeMapper;
        this.languageRepo = languageRepo;
    }

    public List<TypeDTOResponse> getTypes(String langCode) {
        return typeRepo.findAll()
                .stream()
                .map(c -> typeMapper.toResponse(c, langCode))
                .toList();
    }

    public TypeDTOResponse getByID(long id, String langCode) {
        Type type = typeRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
        return typeMapper.toResponse(type, langCode);
    }

    public void insertType(TypeDTORequest dto) {
        if (typeRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Type already exists");
        }

        Type type = new Type();
        type = typeRepo.save(type);

        TypeTranslation translation = new TypeTranslation();
        translation.setName(dto.getName());
        translation.setLanguage(languageRepo.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found")));
        translation.setType(type);
        type.getTranslations().add(translation);

        type = typeRepo.save(type);

        typeMapper.toResponse(type, dto.getLanguageCode());
    }

    public TypeDTOResponse editType(Long id, TypeDTORequest dto) {
        Type type = typeRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));

        TypeTranslation translation = type.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(dto.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Translation not found for this language"));

        translation.setName(dto.getName());
        return typeMapper.toResponse(type, dto.getLanguageCode());
    }

    public void deleteType(Long id) {
        if (!typeRepo.existsById(id)) {
            throw new EntityNotFoundException("Type with ID " + id + " not found");
        }
        typeRepo.deleteById(id);
    }
}
