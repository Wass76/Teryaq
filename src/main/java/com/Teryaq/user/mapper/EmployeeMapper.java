package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.entity.Employee;

public class EmployeeMapper {
    public static Employee toEntity(EmployeeCreateRequestDTO dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPassword(dto.getPassword());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setStatus(dto.getStatus());
        employee.setDateOfHire(dto.getDateOfHire());
        employee.setWorkStart(dto.getWorkStart());
        employee.setWorkEnd(dto.getWorkEnd());
        // email and pharmacy will be set in service
        return employee;
    }

    public static EmployeeResponseDTO toResponseDTO(Employee entity) {
        if (entity == null) return null;
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setStatus(entity.getStatus());
        dto.setDateOfHire(entity.getDateOfHire());
        dto.setWorkStart(entity.getWorkStart());
        dto.setWorkEnd(entity.getWorkEnd());
        dto.setRoleName(entity.getRole() != null ? entity.getRole().getName() : null);
        dto.setPharmacyId(entity.getPharmacy() != null ? entity.getPharmacy().getId() : null);
        return dto;
    }
} 