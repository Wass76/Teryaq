package com.Teryaq.product.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.PharmacyProductTranslation;

@Repository
public interface PharmacyProductTranslationRepo extends JpaRepository<PharmacyProductTranslation, Long> {
    void deleteByProduct(PharmacyProduct product);
} 