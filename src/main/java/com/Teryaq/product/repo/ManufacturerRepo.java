package com.Teryaq.product.repo;

import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerRepo extends JpaRepository<Manufacturer, Long> {
    boolean existsByName(String name);
}
