package com.Teryaq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  
@Builder
@Schema(description = "Product Type Translation Request", example = """
{
  "name": "دواء",
  "languageCode": "ar"
}
""")
public class TypeTranslationDTORequest {

    @Schema(description = "Language code for the translation", example = "ar")
    private String languageCode;
    
    @Schema(description = "Product type name in the target language", example = "دواء")
    private String name;
}