package com.Teryaq.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTORequest {

    @NotBlank(message = "اسم العميل مطلوب")
    private String name;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "رقم الهاتف يجب أن يكون 10-11 رقم")
    private String phoneNumber;

    private String address;

    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "رقم الهوية الوطنية يجب أن يكون 10 أرقام")
    private String nationalId;

    private String notes;

    @Builder.Default
    private boolean isActive = true;
}
