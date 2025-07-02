package com.Teryaq.product.service;


import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.FormDTORequest;
import com.Teryaq.product.dto.FormDTOResponse;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.FormTranslation;
import com.Teryaq.product.mapper.FormMapper;
import com.Teryaq.product.repo.FormRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormService {

    private final FormRepo formRepo;
    private final FormMapper formMapper;
    private final LanguageRepo languageRepo;

    public FormService(FormRepo formRepo, FormMapper formMapper, LanguageRepo languageRepo) {
        this.formRepo = formRepo;
        this.formMapper = formMapper;
        this.languageRepo = languageRepo;
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

    public FormDTOResponse insertForm(FormDTORequest dto) {
        if (formRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Form already exists");
        }

        Form form = new Form();
        form = formRepo.save(form);

        FormTranslation translation = new FormTranslation();
        translation.setName(dto.getName());
        translation.setLanguage(languageRepo.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found")));
        translation.setForm(form);
        form.getTranslations().add(translation);

        form = formRepo.save(form);

        return formMapper.toResponse(form, dto.getLanguageCode());
    }

    public FormDTOResponse editForm(Long id, FormDTORequest dto) {
        Form form = formRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form with ID " + id + " not found"));

        FormTranslation translation = form.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(dto.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Translation not found for this language"));

        translation.setName(dto.getName());
        return formMapper.toResponse(form, dto.getLanguageCode());
    }

    public void deleteForm(Long id) {
        if (!formRepo.existsById(id)) {
            throw new EntityNotFoundException("Form with ID " + id + " not found");
        }
        formRepo.deleteById(id);
    }
}
