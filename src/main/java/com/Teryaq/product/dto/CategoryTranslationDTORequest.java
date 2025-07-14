package com.Teryaq.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  
@Builder
public class CategoryTranslationDTORequest {

    private String name;
    private String languageCode;
    
}