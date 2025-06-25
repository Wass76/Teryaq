package com.Teryaq.user.dto;

import lombok.Data;

@Data
public class PharmacyCreateRequestDTO {
    private String pharmacyName;
    private String licenseNumber;
    private String phoneNumber;
    private String managerPassword;
}