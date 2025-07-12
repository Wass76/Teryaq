package com.Teryaq.user.controller;

import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.dto.CreateWorkingHoursRequestDTO;
import com.Teryaq.user.service.EmployeeService;
import com.Teryaq.user.service.PharmacyService;
import com.Teryaq.user.service.UserService;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody EmployeeCreateRequestDTO dto) {
        Employee manager = (Employee) userService.getCurrentUser();
        Pharmacy pharmacy = manager.getPharmacy();
        EmployeeResponseDTO employee = employeeService.addEmployee(dto, pharmacy);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesInPharmacy() {
        Employee manager = (Employee) userService.getCurrentUser();
        Long pharmacyId = manager.getPharmacy().getId();
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployeesInPharmacy(pharmacyId);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeInPharmacy(
            @PathVariable Long employeeId, 
            @RequestBody EmployeeCreateRequestDTO dto) {
        Employee manager = (Employee) userService.getCurrentUser();
        Long managerPharmacyId = manager.getPharmacy().getId();
        EmployeeResponseDTO employee = employeeService.updateEmployeeInPharmacy(employeeId, dto, managerPharmacyId);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<Void> deleteEmployeeInPharmacy(@PathVariable Long employeeId) {
        Employee manager = (Employee) userService.getCurrentUser();
        Long managerPharmacyId = manager.getPharmacy().getId();
        employeeService.deleteEmployeeInPharmacy(employeeId, managerPharmacyId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{employeeId}/working-hours")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> createWorkingHoursForEmployee(
            @PathVariable Long employeeId,
            @RequestBody CreateWorkingHoursRequestDTO request) {
        Employee manager = (Employee) userService.getCurrentUser();
        Long managerPharmacyId = manager.getPharmacy().getId();

        return ResponseEntity.ok(employeeService.createWorkingHoursForEmployee(employeeId, request, managerPharmacyId));
    }
} 