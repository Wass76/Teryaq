package com.Teryaq.product.service;


import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.ActiveIngredientDTORequest;
import com.Teryaq.product.dto.ActiveIngredientDTOResponse;
import com.Teryaq.product.entity.ActiveIngredient;
import com.Teryaq.product.entity.ActiveIngredientTranslation;
import com.Teryaq.product.mapper.ActiveIngredientMapper;
import com.Teryaq.product.repo.ActiveIngredientRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActiveIngredientService {

    private final ActiveIngredientRepo activeIngredientRepo;
    private final ActiveIngredientMapper activeIngredientMapper;
    private final LanguageRepo languageRepo;

    public ActiveIngredientService(ActiveIngredientRepo activeIngredientRepo, ActiveIngredientMapper activeIngredientMapper, LanguageRepo languageRepo) {
        this.activeIngredientRepo = activeIngredientRepo;
        this.activeIngredientMapper = activeIngredientMapper;
        this.languageRepo = languageRepo;
    }

    public List<ActiveIngredientDTOResponse> getActiveIngredients(String langCode) {
        return activeIngredientRepo.findAll()
                .stream()
                .map(c -> activeIngredientMapper.toResponse(c, langCode))
                .toList();
    }

    public ActiveIngredientDTOResponse getByID(long id, String langCode) {
        ActiveIngredient activeIngredient = activeIngredientRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ActiveIngredient with ID " + id + " not found"));
        return activeIngredientMapper.toResponse(activeIngredient, langCode);
    }

    public ActiveIngredientDTOResponse insertActiveIngredient(ActiveIngredientDTORequest dto) {
        if (activeIngredientRepo.existsByName(dto.getName())) {
            throw new RuntimeException("ActiveIngredient already exists");
        }

        ActiveIngredient activeIngredient = new ActiveIngredient();
        activeIngredient = activeIngredientRepo.save(activeIngredient);

        ActiveIngredientTranslation translation = new ActiveIngredientTranslation();
        translation.setName(dto.getName());
        translation.setLanguage(languageRepo.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found")));
        translation.setActiveIngredient(activeIngredient);
        activeIngredient.getTranslations().add(translation);

        activeIngredient = activeIngredientRepo.save(activeIngredient);

        return activeIngredientMapper.toResponse(activeIngredient, dto.getLanguageCode());
    }

    public ActiveIngredientDTOResponse editActiveIngredient(Long id, ActiveIngredientDTORequest dto) {
        ActiveIngredient activeIngredient = activeIngredientRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ActiveIngredient with ID " + id + " not found"));

        ActiveIngredientTranslation translation = activeIngredient.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(dto.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Translation not found for this language"));

        translation.setName(dto.getName());
        return activeIngredientMapper.toResponse(activeIngredient, dto.getLanguageCode());
    }

    public void deleteActiveIngredient(Long id) {
        if (!activeIngredientRepo.existsById(id)) {
            throw new EntityNotFoundException("ActiveIngredient with ID " + id + " not found");
        }
        activeIngredientRepo.deleteById(id);
    }
}
