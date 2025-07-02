package com.Teryaq.product.repo;

import com.Teryaq.product.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepo extends JpaRepository<Type, Long> {
    boolean existsByName(String name);

}
