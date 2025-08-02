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
@Schema(description = "Manufacturer Translation Request", example = """
{
  "name": "فايزر",
  "languageCode": "ar"
}
""")
public class ManufacturerTranslationDTORequest {

    @Schema(description = "Manufacturer name in the target language", example = "فايزر")
    private String name;
    
    @Schema(description = "Language code for the translation", example = "ar")
    private String languageCode;

}