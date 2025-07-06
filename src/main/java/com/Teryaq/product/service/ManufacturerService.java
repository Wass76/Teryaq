package com.Teryaq.product.service;


import com.Teryaq.product.dto.ManufacturerDTORequest;
import com.Teryaq.product.dto.ManufacturerDTOResponse;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.mapper.ManufacturerMapper;
import com.Teryaq.product.repo.ManufacturerRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManufacturerService {

    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerMapper manufacturerMapper;

    public ManufacturerService(ManufacturerRepo manufacturerRepo,
                               ManufacturerMapper manufacturerMapper) {
        this.manufacturerRepo = manufacturerRepo;
        this.manufacturerMapper = manufacturerMapper;
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

    public ManufacturerDTOResponse insertManufacturer(ManufacturerDTORequest dto,
                                                      String langCode) {
        if (manufacturerRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Manufacturer already exists");
        }
        Manufacturer manufacturer = manufacturerMapper.toEntity(dto);
        Manufacturer saved = manufacturerRepo.save(manufacturer);
        return manufacturerMapper.toResponse(saved, langCode);
    }

    public ManufacturerDTOResponse editManufacturer(Long id, ManufacturerDTORequest dto,
                                                    String langCode) {
        return manufacturerRepo.findById(id).map(existing -> {
            Manufacturer updated = manufacturerMapper.toEntity(dto);
            updated.setId(existing.getId());
            Manufacturer saved = manufacturerRepo.save(updated);
            return manufacturerMapper.toResponse(saved, langCode);
        }).orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
    }

    public void deleteManufacturer(Long id) {
        if (!manufacturerRepo.existsById(id)) {
            throw new EntityNotFoundException("Manufacturer with ID " + id + " not found");
        }
        manufacturerRepo.deleteById(id);
    }
}
