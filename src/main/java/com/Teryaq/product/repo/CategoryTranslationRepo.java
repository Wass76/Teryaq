package com.Teryaq.product.repo;

import com.Teryaq.product.entity.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTranslationRepo extends JpaRepository<CategoryTranslation, Long> {
}
