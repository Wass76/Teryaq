package com.Teryaq.product.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeDTORequest {


//    private Map<String, String> translations;

    private String name;
    private String languageCode;
}
