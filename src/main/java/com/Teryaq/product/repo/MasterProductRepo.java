package com.Teryaq.product.repo;

import com.Teryaq.product.entity.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterProductRepo extends JpaRepository<MasterProduct, Long> {
}
