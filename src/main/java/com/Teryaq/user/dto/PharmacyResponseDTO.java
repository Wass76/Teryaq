package com.Teryaq.user.dto;

import com.Teryaq.user.Enum.PharmacyType;
import lombok.Data;

@Data
public class PharmacyResponseDTO {
    private Long id;
    private String pharmacyName;
    private String licenseNumber;
    private String address;
    private String email;
    private PharmacyType type;
    private String openingHours;
    private String phoneNumber;
    private String managerEmail;
    private String managerFirstName;
    private String managerLastName;
    
    // Account activation status
    private Boolean isActive;
} 