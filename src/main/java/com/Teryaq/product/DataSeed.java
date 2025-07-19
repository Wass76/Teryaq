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


    private void seedCategories() {
        if (categoryRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();
            // Language en = languageRepo.findByCode("en").orElseThrow();

            Category cat1 = new Category();
            cat1.setName("Painkillers");
            cat1 = categoryRepo.save(cat1);

            Category cat2 = new Category();
            cat2.setName("Antibiotics");
            cat2 = categoryRepo.save(cat2);

            Category cat3 = new Category();
            cat3.setName("Sterilizers");
            cat3 = categoryRepo.save(cat3);

            List<CategoryTranslation> translations = List.of(
                    new CategoryTranslation("مسكنات", cat1, ar),
                    new CategoryTranslation( "مضادات حيوية", cat2, ar),
                    new CategoryTranslation( "المعقمات ", cat3, ar)

            );
            categoryTranslationRepo.saveAll(translations);
            System.out.println("✅ Categories seeded");
        }
    }


    private void seedForms() {
        if (formRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();

            Form form1 = new Form();
            form1.setName("Coated Tablets");
            form1 = formRepo.save(form1);

            Form form2 = new Form();
            form2.setName("syrup");
            form2 = formRepo.save(form2);

            Form form3 = new Form();
            form3.setName("Serum");
            form3 = formRepo.save(form3);

            List<FormTranslation> translations = List.of(
                    new FormTranslation("أقراص ملبسة", form1, ar),
                    new FormTranslation("شراب", form2, ar),
                    new FormTranslation("سيروم", form2, ar)

            );
            formTranslationRepo.saveAll(translations);
            System.out.println("✅ Forms seeded");
        }
    }

    private void seedTypes() {
        if (typeRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();

            Type type1 = new Type();
            type1.setName("Medicine");
            type1 = typeRepo.save(type1);

            Type type2 = new Type();
            type2.setName("cosmetic");
            type2 = typeRepo.save(type2);

            Type type3 = new Type();
            type3.setName("Medical supplies");
            type3 = typeRepo.save(type3);

            List<TypeTranslation> translations = List.of(
                    new TypeTranslation( "دواء ", type1, ar),
                    new TypeTranslation( "مستحضر تجميل ", type2, ar),
                    new TypeTranslation( "مسلتزم طبي ", type1, ar)

            );
            typeTranslationRepo.saveAll(translations);
            System.out.println("✅ Types seeded");
        }
    }

    private void seedManufacturers() {
        if (manufacturerRepo.count() == 0) {
            Language ar = languageRepo.findByCode("ar").orElseThrow();

            Manufacturer m1 = new Manufacturer();
            m1.setName("Teryaq Pharma");
            m1 = manufacturerRepo.save(m1);

            Manufacturer m2 = new Manufacturer();
            m2.setName("Ultra Medica");
            m2 = manufacturerRepo.save(m2);

            Manufacturer m3 = new Manufacturer();
            m3.setName("Avenzor");
            m3 = manufacturerRepo.save(m3);

            List<ManufacturerTranslation> translations = List.of(
                    new ManufacturerTranslation("ترياق فارما", m1, ar),
                    new ManufacturerTranslation("ألترا ميديكا", m2, ar),
                    new ManufacturerTranslation("ابن زهر", m3, ar)
            );
            manufacturerTranslationRepo.saveAll(translations);
            System.out.println("✅ Manufacturers seeded");
        }
    }



}

