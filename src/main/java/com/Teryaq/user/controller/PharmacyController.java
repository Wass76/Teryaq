package com.Teryaq.user.controller;

import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.EmployeeCreateRequestDTO;
import com.Teryaq.user.dto.EmployeeResponseDTO;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.service.PharmacyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping("/login")
    public ResponseEntity<UserAuthenticationResponse> managerLogin(@RequestBody AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = pharmacyService.managerLogin(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-registration")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> completeRegistration(
            @RequestParam String newPassword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String managerFirstName,
            @RequestParam(required = false) String managerLastName,
            @RequestParam(required = false) String pharmacyPhone,
            @RequestParam(required = false) String pharmacyEmail,
            @RequestParam(required = false) String openingHours
    ) {
        PharmacyResponseDTO pharmacy = pharmacyService.completeRegistration(newPassword, location, managerFirstName,managerLastName, pharmacyPhone , pharmacyEmail, openingHours);
        return ResponseEntity.ok(pharmacy);
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeCreateRequestDTO dto) {
        EmployeeResponseDTO employee = pharmacyService.addEmployee(dto);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<PharmacyResponseDTO>> getAllPharmacies() {
        List<PharmacyResponseDTO> pharmacies = pharmacyService.getAllPharmacies();
        return ResponseEntity.ok(pharmacies);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesInPharmacy() {
        return ResponseEntity.ok(pharmacyService.getAllEmployeesInPharmacy());
    }

    @PutMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeInPharmacy(@PathVariable Long employeeId, @RequestBody EmployeeCreateRequestDTO dto) {
        return ResponseEntity.ok(pharmacyService.updateEmployeeInPharmacy(employeeId, dto));
    }

    @DeleteMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    public ResponseEntity<Void> deleteEmployeeInPharmacy(@PathVariable Long employeeId) {
        pharmacyService.deleteEmployeeInPharmacy(employeeId);
        return ResponseEntity.noContent().build();
    }
} 