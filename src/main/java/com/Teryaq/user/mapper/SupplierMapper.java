package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.SupplierDTORequest;
import com.Teryaq.user.dto.SupplierDTOResponse;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.entity.Pharmacy;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    public Supplier toEntity(SupplierDTORequest dto, Pharmacy pharmacy) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setPreferredCurrency(dto.getPreferredCurrency());
        supplier.setPharmacy(pharmacy);
        return supplier;
    }

    public SupplierDTOResponse toResponse(Supplier supplier) {
        SupplierDTOResponse dto = new SupplierDTOResponse();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setPreferredCurrency(supplier.getPreferredCurrency());
        return dto;
    }

    public void updateEntity(Supplier supplier, SupplierDTORequest dto) {
        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setPreferredCurrency(dto.getPreferredCurrency());
    }
} 