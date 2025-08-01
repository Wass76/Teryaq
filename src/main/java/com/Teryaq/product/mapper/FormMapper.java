package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.FormDTORequest;
import com.Teryaq.product.dto.FormDTOResponse;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.FormTranslation;
import org.springframework.stereotype.Component;

@Component
public class FormMapper {


   public FormDTOResponse toResponse(Form form, String langCode) {
       if (form == null) return null;

       String sanitizedLangCode = langCode == null ? "en" : langCode.trim().toLowerCase();
       
       

       String translatedName = form.getTranslations().stream()
               .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
               .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedLangCode))
               .map(FormTranslation::getName)
               .findFirst()
               .orElse(form.getName());
               
       

     return FormDTOResponse.builder()
             .id(form.getId())
             .name(translatedName)
             .build();
 }

    public Form toEntity(FormDTORequest dto) {
        if (dto == null) return null;

        Form form = new Form();
        form.setName(dto.getName());
        return form;
    }

}
