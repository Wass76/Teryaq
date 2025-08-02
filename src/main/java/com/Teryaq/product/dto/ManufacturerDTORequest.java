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
@Schema(description = "Manufacturer Request", example = """
{
  "name": "Pfizer",
  "languageCode": "en",
  "translations": [
    {
      "name": "فايزر",
      "languageCode": "ar"
    }
  ]
}
""")
public class ManufacturerDTORequest {

    @Schema(description = "Manufacturer name", example = "Pfizer")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String languageCode;

    @Schema(description = "Manufacturer translations for different languages")
    private Set<ManufacturerTranslationDTORequest> translations;

}
