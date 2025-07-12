package com.Teryaq.user.controller;

import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import com.Teryaq.user.dto.UserAuthenticationResponse;
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

    @GetMapping("/all")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<PharmacyResponseDTO>> getAllPharmacies() {
        List<PharmacyResponseDTO> pharmacies = pharmacyService.getAllPharmacies();
        return ResponseEntity.ok(pharmacies);
    }
} 