package com.Teryaq.product.repo;

import com.Teryaq.product.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepo extends JpaRepository<Form, Long> {
    boolean existsByName(String name);
}
