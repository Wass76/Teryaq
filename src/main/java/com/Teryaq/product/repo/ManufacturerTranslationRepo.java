package com.Teryaq.product.repo;

import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.ManufacturerTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerTranslationRepo extends JpaRepository<ManufacturerTranslation, Long> {
    boolean existsByName(String name);
}
