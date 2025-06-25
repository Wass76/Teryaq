package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.PharmacyCreateRequestDTO;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;

public class PharmacyMapper {
    public static Pharmacy toEntity(PharmacyCreateRequestDTO dto) {
        if (dto == null) return null;
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getPharmacyName());
        pharmacy.setLicenseNumber(dto.getLicenseNumber());
        pharmacy.setPhoneNumber(dto.getPhoneNumber());
        return pharmacy;
    }

    public static PharmacyCreateRequestDTO toDto(Pharmacy entity) {
        if (entity == null) return null;
        PharmacyCreateRequestDTO dto = new PharmacyCreateRequestDTO();
        dto.setPharmacyName(entity.getName());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setPhoneNumber(entity.getPhoneNumber());
        // managerPassword not set from entity
        return dto;
    }

    public static PharmacyResponseDTO toResponseDTO(Pharmacy pharmacy, Employee manager) {
        if (pharmacy == null) return null;
        PharmacyResponseDTO dto = new PharmacyResponseDTO();
        dto.setId(pharmacy.getId());
        dto.setPharmacyName(pharmacy.getName());
        dto.setLicenseNumber(pharmacy.getLicenseNumber());
        dto.setAddress(pharmacy.getAddress());
        dto.setEmail(pharmacy.getEmail());
        dto.setType(pharmacy.getType());
        dto.setOpeningHours(pharmacy.getOpeningHours());
        dto.setPhoneNumber(pharmacy.getPhoneNumber());
        if (manager != null) {
            dto.setManagerEmail(manager.getEmail());
            dto.setManagerFirstName(manager.getFirstName());
            dto.setManagerLastName(manager.getLastName());
        }
        return dto;
    }

    public static void updatePharmacyFromRequest(Pharmacy pharmacy, String address, String email, String openingHours) {
        if (address != null && !address.isEmpty()) {
            pharmacy.setAddress(address);
        }
        if (email != null && !email.isEmpty()) {
            pharmacy.setEmail(email);
        }
        if (openingHours != null && !openingHours.isEmpty()) {
            pharmacy.setOpeningHours(openingHours);
        }
    }
} 