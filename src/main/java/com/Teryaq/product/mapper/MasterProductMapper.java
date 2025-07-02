package com.Teryaq.product.mapper;

import com.Teryaq.product.dto.MProductDTORequest;
import com.Teryaq.product.dto.MProductDTOResponse;
import com.Teryaq.product.entity.*;
import com.Teryaq.product.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MasterProductMapper {

    private final CategoryRepo categoryRepo;
    private final ActiveIngredientRepo activeIngredientRepo;
    private final TypeRepo typeRepo;
    private final FormRepo formRepo;
    private final ManufacturerRepo manufacturerRepo;

    public MasterProduct toEntity(MProductDTORequest dto) {
        MasterProduct product = new MasterProduct();

        product.setTradeName(dto.getTradeName());
        product.setScientificName(dto.getScientificName());
        product.setConcentration(dto.getConcentration());
        product.setSize(dto.getSize());
        product.setRefPurchasePrice(dto.getRefPurchasePrice());
        product.setRefSellingPrice(dto.getRefSellingPrice());
        product.setNotes(dto.getNotes());
        product.setTax(dto.getTax());
        product.setBarcode(dto.getBarcode());
        product.setRequiresPrescription(dto.getRequiresPrescription());

        if (dto.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepo.findAllById(dto.getCategoryIds())));
        }

        if (dto.getActiveIngredientIds() != null) {
            product.setActiveIngredients(new HashSet<>(activeIngredientRepo.findAllById(dto.getActiveIngredientIds())));
        }

        if (dto.getTypeId() != null)
            product.setType(typeRepo.findById(dto.getTypeId()).orElse(null));

        if (dto.getFormId() != null)
            product.setForm(formRepo.findById(dto.getFormId()).orElse(null));

        if (dto.getManufacturerId() != null)
            product.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));

        return product;
    }

    public MProductDTOResponse toResponse(MasterProduct product, String languageCode) {
        MasterProductTranslation translation = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                .findFirst()
                .orElse(null)
                : null;

        return MProductDTOResponse.builder()
                .id(product.getId())
                .tradeName(translation != null ? translation.getTradeName() : product.getTradeName())
                .scientificName(translation != null ? translation.getScientificName() : product.getScientificName())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .refPurchasePrice(product.getRefPurchasePrice())
                .refSellingPrice(product.getRefSellingPrice())
                .notes(translation != null ? translation.getNotes() : product.getNotes())
                .tax(product.getTax())
                .barcode(product.getBarcode())
                .dataSource(product.getDataSource())
                .requiresPrescription(product.getRequiresPrescription())

                .categories(
                        product.getCategories().stream()
                                .map(category -> {
                                    if (category.getTranslations() == null) return category.getName();
                                    return category.getTranslations().stream()
                                            .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                            .findFirst()
                                            .map(CategoryTranslation::getName)
                                            .orElse(category.getName());
                                })
                                .collect(Collectors.toSet())
                )

                .activeIngredients(
                        product.getActiveIngredients().stream()
                                .map(active -> {
                                    if (active.getTranslations() == null) return active.getName();
                                    return active.getTranslations().stream()
                                            .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                            .findFirst()
                                            .map(ActiveIngredientTranslation::getName)
                                            .orElse(active.getName());
                                })
                                .collect(Collectors.toSet())
                )

                .type(
                        product.getType() != null
                                ? product.getType().getTranslations().stream()
                                .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(TypeTranslation::getName)
                                .orElse(product.getType().getName())
                                : null
                )

                .form(
                        product.getForm() != null
                                ? product.getForm().getTranslations().stream()
                                .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(FormTranslation::getName)
                                .orElse(product.getForm().getName())
                                : null
                )

                .manufacturer(
                        product.getManufacturer() != null
                                ? product.getManufacturer().getTranslations().stream()
                                .filter(t -> languageCode.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(ManufacturerTranslation::getName)
                                .orElse(product.getManufacturer().getName())
                                : null
                )

                .build();
    }


}
