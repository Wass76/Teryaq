package com.Teryaq.product.aPharmacyProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyProductTranslationRepo extends JpaRepository<PharmacyProductTranslation, Long> {
    void deleteByProduct(PharmacyProduct product);
} 