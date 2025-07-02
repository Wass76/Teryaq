package com.Teryaq.product;

import com.Teryaq.language.Language;
import com.Teryaq.language.LanguageRepo;
import com.Teryaq.product.repo.*;
import com.Teryaq.product.entity.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeed {

    private final LanguageRepo languageRepo;

    private final ActiveIngredientRepo activeIngredientRepo;
    private final ActiveIngredientTranslationRepo activeIngredientTranslationRepo;

    private final CategoryRepo categoryRepo;
    private final CategoryTranslationRepo categoryTranslationRepo;

    private final FormRepo formRepo;
    private final FormTranslationRepo formTranslationRepo;

    private final TypeRepo typeRepo;
    private final TypeTranslationRepo typeTranslationRepo;

    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerTranslationRepo manufacturerTranslationRepo;

    @PostConstruct
    public void seedAll() {
        seedLanguages();
        seedActiveIngredients();
        seedCategories();
        seedForms();
        seedTypes();
        seedManufacturers();
    }

    private void seedLanguages() {
        if (languageRepo.count() == 0) {
            List<Language> languages = List.of(
                    new Language("ar", "Arabic"),
                    new Language("en", "English")
            );
            languageRepo.saveAll(languages);
            System.out.println("✅ Languages seeded");
        }
    }

    private void seedActiveIngredients() {
        if (activeIngredientRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            Language en = languageRepo.findByCode("en").orElseThrow();

            ActiveIngredient ing1 = activeIngredientRepo.save(new ActiveIngredient(null, "paracetamol", null, null, null, null));
            ActiveIngredient ing2 = activeIngredientRepo.save(new ActiveIngredient(null, "ibuprofen", null, null, null, null));

            List<ActiveIngredientTranslation> translations = List.of(
                    new ActiveIngredientTranslation(null, "باراسيتامول", ing1, ar),
                    new ActiveIngredientTranslation(null, "Paracetamol", ing1, en),
                    new ActiveIngredientTranslation(null, "إيبوبروفين", ing2, ar),
                    new ActiveIngredientTranslation(null, "Ibuprofen", ing2, en)
            );
            activeIngredientTranslationRepo.saveAll(translations);
            System.out.println("✅ Active Ingredients seeded");
        }
    }

    private void seedCategories() {
        if (categoryRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            Language en = languageRepo.findByCode("en").orElseThrow();

            Category cat1 = new Category();
            cat1.setName("Painkillers");
            cat1 = categoryRepo.save(cat1);

            Category cat2 = new Category();
            cat2.setName("Antibiotics");
            cat2 = categoryRepo.save(cat2);

            List<CategoryTranslation> translations = List.of(
                    new CategoryTranslation(null, "مسكنات", cat1, ar),
                    new CategoryTranslation(null, "Painkillers", cat1, en),
                    new CategoryTranslation(null, "مضادات حيوية", cat2, ar),
                    new CategoryTranslation(null, "Antibiotics", cat2, en)
            );
            categoryTranslationRepo.saveAll(translations);
            System.out.println("✅ Categories seeded");
        }
    }


    private void seedForms() {
        if (formRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            Language en = languageRepo.findByCode("en").orElseThrow();

            Form form = new Form();
            form.setName("Tablet");
            form = formRepo.save(form);

            List<FormTranslation> translations = List.of(
                    new FormTranslation(null, "حبوب", form, ar),
                    new FormTranslation(null, "Tablet", form, en)
            );
            formTranslationRepo.saveAll(translations);
            System.out.println("✅ Forms seeded");
        }
    }

    private void seedTypes() {
        if (typeRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            Language en = languageRepo.findByCode("en").orElseThrow();

            Type type = new Type();
            type.setName("Generic");
            type = typeRepo.save(type);

            List<TypeTranslation> translations = List.of(
                    new TypeTranslation(null, "دواء جنيس", type, ar),
                    new TypeTranslation(null, "Generic", type, en)
            );
            typeTranslationRepo.saveAll(translations);
            System.out.println("✅ Types seeded");
        }
    }

    private void seedManufacturers() {
        if (manufacturerRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            Language en = languageRepo.findByCode("en").orElseThrow();

            Manufacturer m = new Manufacturer();
            m.setName("Teryaq Pharma");
            m = manufacturerRepo.save(m);

            List<ManufacturerTranslation> translations = List.of(
                    new ManufacturerTranslation(null, "ترياق فارما", m, ar),
                    new ManufacturerTranslation(null, "Teryaq Pharma", m, en)
            );
            manufacturerTranslationRepo.saveAll(translations);
            System.out.println("✅ Manufacturers seeded");
        }
    }



}

