package com.Teryaq.product.repo;

import com.Teryaq.product.entity.TypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeTranslationRepo extends JpaRepository<TypeTranslation, Long> {
    boolean existsByName(String name);

}
