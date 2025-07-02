package com.Teryaq.product.repo;

import com.Teryaq.product.entity.ActiveIngredient;
import com.Teryaq.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveIngredientRepo extends JpaRepository<ActiveIngredient, Long> {
    boolean existsByName(String name);
}
