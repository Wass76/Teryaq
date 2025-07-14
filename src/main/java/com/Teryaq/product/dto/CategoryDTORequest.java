package com.Teryaq.product.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTORequest {


    private String name;
    private String languageCode;

    private Set<CategoryTranslationDTORequest> translations;
}   
