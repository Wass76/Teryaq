package com.Teryaq.user.dto;

import com.Teryaq.user.Enum.UserStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private LocalDate dateOfHire;
    private LocalTime workStart;
    private LocalTime workEnd;
    private String roleName;
    private Long pharmacyId;
} 