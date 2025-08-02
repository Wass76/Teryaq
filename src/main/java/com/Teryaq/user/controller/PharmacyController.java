package com.Teryaq.user.controller;

import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.PharmacyResponseDTO;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import com.Teryaq.user.service.PharmacyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
@Tag(name = "Pharmacy Management", description = "APIs for managing pharmacy operations and authentication")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping("/login")
    @Operation(
        summary = "Pharmacy manager login",
        description = "Authenticates a pharmacy manager and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> managerLogin(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = pharmacyService.managerLogin(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-registration")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Complete pharmacy registration",
        description = "Completes the pharmacy registration process with additional information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration completed successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> completeRegistration(
            @Parameter(description = "New password for the pharmacy manager") 
            @RequestParam String newPassword,
            @Parameter(description = "Pharmacy location") 
            @RequestParam(required = false) String location,
            @Parameter(description = "Manager's first name") 
            @RequestParam(required = false) String managerFirstName,
            @Parameter(description = "Manager's last name") 
            @RequestParam(required = false) String managerLastName,
            @Parameter(description = "Pharmacy phone number") 
            @RequestParam(required = false) String pharmacyPhone,
            @Parameter(description = "Pharmacy email address") 
            @RequestParam(required = false) String pharmacyEmail,
            @Parameter(description = "Pharmacy opening hours") 
            @RequestParam(required = false) String openingHours
    ) {
        PharmacyResponseDTO pharmacy = pharmacyService.completeRegistration(newPassword, location, managerFirstName,managerLastName, pharmacyPhone , pharmacyEmail, openingHours);
        return ResponseEntity.ok(pharmacy);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all pharmacies",
        description = "Retrieves a list of all pharmacies in the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all pharmacies",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PharmacyResponseDTO>> getAllPharmacies() {
        List<PharmacyResponseDTO> pharmacies = pharmacyService.getAllPharmacies();
        return ResponseEntity.ok(pharmacies);
    }
} 