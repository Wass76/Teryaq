package com.Teryaq.product.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormDTORequest {


    private String name;
    private String languageCode;

    private Set<FormTranslationDTORequest> translations;

}
