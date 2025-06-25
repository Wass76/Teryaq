package com.Teryaq.user.dto;

import com.Teryaq.user.Enum.UserStatus;
import com.Teryaq.utils.annotation.ValidEnum;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EmployeeCreateRequestDTO {
    private String firstName;
    private String lastName;
    private String password;
    private String phoneNumber;
    @ValidEnum(enumClass = UserStatus.class)
    private UserStatus status;
    private LocalDate dateOfHire;
    private Long roleId;
    private LocalTime workStart;
    private LocalTime workEnd;
} 