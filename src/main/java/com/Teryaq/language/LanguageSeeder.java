package com.Teryaq.language;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

    @Component
    @RequiredArgsConstructor
    public class LanguageSeeder {

        private final LanguageRepo languageRepo;

        @PostConstruct
        public void seedLanguages() {
            if (languageRepo.count() == 0) {
                List<Language> languages = List.of(
                        new Language( "ar", "Arabic"),
                        new Language( "en", "English")
                );
                languageRepo.saveAll(languages);
                System.out.println("✅ Languages seeded");
            }
        }
    }


