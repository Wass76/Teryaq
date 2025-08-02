package com.Teryaq.user.controller;

import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.dto.CreateWorkingHoursRequestDTO;
import com.Teryaq.user.service.EmployeeService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody EmployeeCreateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.addEmployee(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesInPharmacy() {
        return ResponseEntity.ok(employeeService.getAllEmployeesInPharmacy());
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeInPharmacy(
            @PathVariable Long employeeId, 
            @RequestBody EmployeeCreateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployeeInPharmacy(employeeId, dto));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<Void> deleteEmployeeInPharmacy(@PathVariable Long employeeId) {
        employeeService.deleteEmployeeInPharmacy(employeeId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{employeeId}/working-hours")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> createWorkingHoursForEmployee(
            @PathVariable Long employeeId,
            @RequestBody CreateWorkingHoursRequestDTO request) {
        return ResponseEntity.ok(employeeService.createWorkingHoursForEmployee(employeeId, request));
    }
} 