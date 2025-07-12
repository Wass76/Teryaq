package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.dto.EmployeeWorkingHoursDTO;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.EmployeeWorkingHours;

import java.util.List;
import java.util.stream.Collectors;

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
        // Don't set working hours here - will be handled in service after employee is saved
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
        dto.setRoleName(entity.getRole() != null ? entity.getRole().getName() : null);
        dto.setPharmacyId(entity.getPharmacy() != null ? entity.getPharmacy().getId() : null);
        
        // Set working hours if available
        if (entity.getEmployeeWorkingHoursList() != null) {
            dto.setWorkingHours(EmployeeWorkingHoursMapper.toDTOList(entity.getEmployeeWorkingHoursList()));
        }
        
        return dto;
    }
    
    public static List<EmployeeWorkingHours> createWorkingHoursFromDTO(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs == null || workingHoursDTOs.isEmpty()) {
            return null;
        }
        
        return workingHoursDTOs.stream()
                .map(dto -> {
                    EmployeeWorkingHours workingHours = EmployeeWorkingHoursMapper.toEntity(dto);
                    workingHours.setEmployee(employee);
                    return workingHours;
                })
                .collect(Collectors.toList());
    }
} 