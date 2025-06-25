package com.Teryaq.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Schema
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
    private String ConfirmPassword;
}
