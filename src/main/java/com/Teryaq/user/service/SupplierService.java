package com.Teryaq.user.service;

import com.Teryaq.user.dto.SupplierDTORequest;
import com.Teryaq.user.dto.SupplierDTOResponse;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.mapper.SupplierMapper;
import com.Teryaq.user.repository.SupplierRepository;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import com.Teryaq.utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierDTOResponse create(SupplierDTORequest request) {
        if (supplierRepository.findAll().stream().anyMatch(s -> s.getName().equals(request.getName()))) {
            throw new ConflictException("Supplier name must be unique");
        }
        Supplier supplier = supplierMapper.toEntity(request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDTOResponse update(Long id, SupplierDTORequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        if (!supplier.getName().equals(request.getName()) && supplierRepository.findAll().stream().anyMatch(s -> s.getName().equals(request.getName()))) {
            throw new ConflictException("Supplier name must be unique");
        }
        supplierMapper.updateEntity(supplier, request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        supplierRepository.delete(supplier);
    }

    public SupplierDTOResponse getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        return supplierMapper.toResponse(supplier);
    }

    public List<SupplierDTOResponse> listAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SupplierDTOResponse> searchByName(String name) {
        return supplierRepository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
} 