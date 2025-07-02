package com.Teryaq.product.service;


import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.ManufacturerTranslation;
import com.Teryaq.product.mapper.ManufacturerMapper;
import com.Teryaq.product.repo.ManufacturerRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManufacturerService {

    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerMapper manufacturerMapper;
    private final LanguageRepo languageRepo;

    public ManufacturerService(ManufacturerRepo manufacturerRepo, ManufacturerMapper manufacturerMapper, LanguageRepo languageRepo) {
        this.manufacturerRepo = manufacturerRepo;
        this.manufacturerMapper = manufacturerMapper;
        this.languageRepo = languageRepo;
    }

    public List<ManufacturerDTOResponse> getManufacturers(String langCode) {
        return manufacturerRepo.findAll()
                .stream()
                .map(c -> manufacturerMapper.toResponse(c, langCode))
                .toList();
    }

    public ManufacturerDTOResponse getByID(long id, String langCode) {
        Manufacturer manufacturer = manufacturerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
        return manufacturerMapper.toResponse(manufacturer, langCode);
    }

    public ManufacturerDTOResponse insertManufacturer(ManufacturerDTORequest dto) {
        if (manufacturerRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Manufacturer already exists");
        }

        Manufacturer manufacturer = new Manufacturer();
        manufacturer = manufacturerRepo.save(manufacturer);

        ManufacturerTranslation translation = new ManufacturerTranslation();
        translation.setName(dto.getName());
        translation.setLanguage(languageRepo.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found")));
        translation.setManufacturer(manufacturer);
        manufacturer.getTranslations().add(translation);

        manufacturer = manufacturerRepo.save(manufacturer);

        return manufacturerMapper.toResponse(manufacturer, dto.getLanguageCode());
    }

    public ManufacturerDTOResponse editManufacturer(Long id, ManufacturerDTORequest dto) {
        Manufacturer manufacturer = manufacturerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));

        ManufacturerTranslation translation = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equalsIgnoreCase(dto.getLanguageCode()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Translation not found for this language"));

        translation.setName(dto.getName());
        return manufacturerMapper.toResponse(manufacturer, dto.getLanguageCode());
    }

    public void deleteManufacturer(Long id) {
        if (!manufacturerRepo.existsById(id)) {
            throw new EntityNotFoundException("Manufacturer with ID " + id + " not found");
        }
        manufacturerRepo.deleteById(id);
    }
}
