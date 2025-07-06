package com.Teryaq.product.service;

import com.Teryaq.product.dto.TypeDTORequest;
import com.Teryaq.product.dto.TypeDTOResponse;
import com.Teryaq.product.entity.Type;
import com.Teryaq.product.mapper.TypeMapper;
import com.Teryaq.product.repo.TypeRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeService {

    private final TypeRepo typeRepo;
    private final TypeMapper typeMapper;

    public TypeService(TypeRepo typeRepo, TypeMapper typeMapper) {
        this.typeRepo = typeRepo;
        this.typeMapper = typeMapper;
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

    public TypeDTOResponse insertType(TypeDTORequest dto, String langCode) {
        if (typeRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Type already exists");
        }
        Type type = typeMapper.toEntity(dto);
        Type saved = typeRepo.save(type);
        return typeMapper.toResponse(saved, langCode);
    }

    public TypeDTOResponse editType(Long id, TypeDTORequest dto, String langCode) {
        return typeRepo.findById(id).map(existing -> {
            Type updated = typeMapper.toEntity(dto);
            updated.setId(existing.getId());
            Type saved = typeRepo.save(updated);
            return typeMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
    }

    public void deleteType(Long id) {
        if (!typeRepo.existsById(id)) {
            throw new EntityNotFoundException("Type with ID " + id + " not found");
        }
        typeRepo.deleteById(id);
    }
}
