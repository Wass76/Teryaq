package com.Teryaq.product.service;


import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.ManufacturerTranslation;
import com.Teryaq.product.mapper.ManufacturerMapper;
import com.Teryaq.product.repo.ManufacturerRepo;
import com.Teryaq.product.repo.ManufacturerTranslationRepo;
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
public class ManufacturerService {

    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerMapper manufacturerMapper;
    private final LanguageRepo languageRepo;
    private final ManufacturerTranslationRepo manufacturerTranslationRepo;

    public ManufacturerService(ManufacturerRepo manufacturerRepo,
                               ManufacturerMapper manufacturerMapper,
                               LanguageRepo languageRepo,
                               ManufacturerTranslationRepo manufacturerTranslationRepo) {
        this.manufacturerRepo = manufacturerRepo;
        this.manufacturerMapper = manufacturerMapper;
        this.languageRepo = languageRepo;
        this.manufacturerTranslationRepo = manufacturerTranslationRepo;
    }

    public List<ManufacturerDTOResponse> getManufacturers(String langCode) {
        log.info("Getting manufacturers with langCode: {}", langCode);
        List<Manufacturer> manufacturers = manufacturerRepo.findAllWithTranslations();
        log.info("Found {} manufacturers", manufacturers.size());
        
        return manufacturers.stream()
                .map(manufacturer -> {
                    log.info("Processing manufacturer: {} with {} translations", manufacturer.getName(),
                            manufacturer.getTranslations() != null ? manufacturer.getTranslations().size() : 0);
                    return manufacturerMapper.toResponse(manufacturer, langCode);
                })
                .toList();
    }

    public ManufacturerDTOResponse getByID(long id, String langCode) {
        Manufacturer manufacturer = manufacturerRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
        return manufacturerMapper.toResponse(manufacturer, langCode);
    }

    public ManufacturerDTOResponse insertManufacturer(ManufacturerDTORequest dto,
                                                      String langCode) {
        if (manufacturerRepo.existsByName(dto.getName())) {
            throw new ConflictException("Manufacturer with name '" + dto.getName() + "' already exists");
        }
        
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.getName());
        Manufacturer savedManufacturer = manufacturerRepo.save(manufacturer);

        List<ManufacturerTranslation> translations = dto.getTranslations().stream()
            .map(t -> {
                Language lang = languageRepo.findByCode(t.getLanguageCode())
                        .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                return new ManufacturerTranslation(null, t.getName(), savedManufacturer, lang);
            })
            .collect(Collectors.toList());

        manufacturerTranslationRepo.saveAll(translations);
        savedManufacturer.setTranslations(new HashSet<>(translations));

        return manufacturerMapper.toResponse(savedManufacturer, langCode);
    }

    public ManufacturerDTOResponse editManufacturer(Long id, ManufacturerDTORequest dto,
                                                    String langCode) {
        return manufacturerRepo.findByIdWithTranslations(id).map(existing -> {
            if (!existing.getName().equals(dto.getName()) && manufacturerRepo.existsByName(dto.getName())) {
                throw new ConflictException("Manufacturer with name '" + dto.getName() + "' already exists");
            }

            existing.setName(dto.getName());
            Manufacturer saved = manufacturerRepo.save(existing);

            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                manufacturerTranslationRepo.deleteByManufacturer(saved);

                List<ManufacturerTranslation> translations = dto.getTranslations().stream()
                        .map(t -> {
                            Language lang = languageRepo.findByCode(t.getLanguageCode())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLanguageCode()));
                            return new ManufacturerTranslation(null, t.getName(), saved, lang);
                        })
                        .toList();

                manufacturerTranslationRepo.saveAll(translations);
            }

            // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
            Manufacturer updated = manufacturerRepo.findByIdWithTranslations(saved.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Updated manufacturer not found"));

            return manufacturerMapper.toResponse(updated, langCode);

        }).orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
    }

    public void deleteManufacturer(Long id) {
        if (!manufacturerRepo.existsById(id)) {
            throw new EntityNotFoundException("Manufacturer with ID " + id + " not found");
        }
        manufacturerRepo.deleteById(id);
    }
}
