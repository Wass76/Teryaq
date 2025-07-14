package com.Teryaq.language;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/languages")
public class LanguageController {

    private LanguageService languageService;

    @Autowired
    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    public List<Language> getLanguages() {
        return languageService.gitAll();
    }

    @GetMapping("{id}")
    public Language getLanguage(@PathVariable Long id) {
        return languageService.gitById(id);
    }

//    @PostMapping
//    public void addLanguage(@RequestBody Language language) {
//         languageService.createLanguage(language);
//    }
//
//    @PutMapping("{id}")
//    public Language updateLanguage(@PathVariable Long id, @RequestBody Language language) {
//        return languageService.editLanguage(id, language);
//    }

//    @DeleteMapping("{id}")
//    public void deleteLanguage(@PathVariable Long id) {
//         languageService.deleteLanguage(id);
//    }

}
