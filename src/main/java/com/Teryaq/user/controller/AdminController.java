package com.Teryaq.user.controller;

import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.PharmacyCreateRequestDTO;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import com.Teryaq.user.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping("/login")
    public ResponseEntity<UserAuthenticationResponse> adminLogin(@RequestBody AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = pharmacyService.adminLogin(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pharmacies")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createPharmacy(@RequestBody PharmacyCreateRequestDTO dto) {
        PharmacyResponseDTO pharmacy = pharmacyService.createPharmacy(dto);
        return ResponseEntity.ok(pharmacy);
    }
} 