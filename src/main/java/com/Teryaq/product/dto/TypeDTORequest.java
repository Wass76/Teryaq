package com.Teryaq.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Product Type Request", example = """
{
  "name": "Medicine",
  "languageCode": "en",
  "translations": [
    {
      "name": "دواء",
      "languageCode": "ar"
    }
  ]
}
""")
public class TypeDTORequest {

    @Schema(description = "Product type name", example = "Medicine")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String languageCode;

    @Schema(description = "Product type translations for different languages")
    private Set<TypeTranslationDTORequest> translations;

}
