package com.Teryaq.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDebtDTORequest {
    private Long customerId;
    private Float amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String notes;
} 