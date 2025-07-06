package com.Teryaq.product.service;


import com.Teryaq.product.dto.FormDTORequest;
import com.Teryaq.product.dto.FormDTOResponse;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.mapper.FormMapper;
import com.Teryaq.product.repo.FormRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormService {

    private final FormRepo formRepo;
    private final FormMapper formMapper;

    public FormService(FormRepo formRepo, FormMapper formMapper) {
        this.formRepo = formRepo;
        this.formMapper = formMapper;
    }

    public List<FormDTOResponse> getForms(String langCode) {
        return formRepo.findAll()
                .stream()
                .map(c -> formMapper.toResponse(c, langCode))
                .toList();
    }

    public FormDTOResponse getByID(long id, String langCode) {
        Form form = formRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form with ID " + id + " not found"));
        return formMapper.toResponse(form, langCode);
    }

    public FormDTOResponse insertForm(FormDTORequest dto, String langCode) {
        if (formRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Category already exists");
        }
        Form form = formMapper.toEntity(dto);
        Form saved = formRepo.save(form);
        return formMapper.toResponse(saved, langCode);
    }

    public FormDTOResponse editForm(Long id, FormDTORequest dto, String langCode) {
        return formRepo.findById(id).map(existing -> {
            Form updated = formMapper.toEntity(dto);
            updated.setId(existing.getId());
            Form saved = formRepo.save(updated);
            return formMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    public void deleteForm(Long id) {
        if (!formRepo.existsById(id)) {
            throw new EntityNotFoundException("Form with ID " + id + " not found");
        }
        formRepo.deleteById(id);
    }
}
