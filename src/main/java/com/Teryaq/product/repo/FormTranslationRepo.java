package com.Teryaq.product.repo;

import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.FormTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormTranslationRepo extends JpaRepository<FormTranslation, Long> {
    void deleteByForm(Form form);
}
