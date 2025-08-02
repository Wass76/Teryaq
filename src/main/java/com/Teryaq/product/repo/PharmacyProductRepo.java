package com.Teryaq.product.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Teryaq.product.entity.PharmacyProduct;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyProductRepo extends JpaRepository<PharmacyProduct, Long> {
    // البحث عن منتجات صيدلية معينة
    Page<PharmacyProduct> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM PharmacyProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language")
    List<PharmacyProduct> findAllWithTranslations();
    
    @Query("SELECT DISTINCT p FROM PharmacyProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language WHERE p.id = :id")
    Optional<PharmacyProduct> findByIdWithTranslations(@Param("id") Long id);
    
    @Query("""
    SELECT DISTINCT p FROM PharmacyProduct p
    LEFT JOIN p.translations pt
    LEFT JOIN p.barcodes pb
    WHERE (
        LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(pb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (pt.language.code = :languageCode AND (
            LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
    )
    """)
    Page<PharmacyProduct> search(
            @Param("keyword") String keyword,
            @Param("languageCode") String languageCode,
            Pageable pageable);
    
    @Query("""
    SELECT DISTINCT p FROM PharmacyProduct p
    LEFT JOIN p.translations pt
    LEFT JOIN p.barcodes pb
    WHERE p.pharmacy.id = :pharmacyId AND (
        LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(pb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (pt.language.code = :languageCode AND (
            LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
    )
    """)
    Page<PharmacyProduct> searchByPharmacyId(
            @Param("keyword") String keyword,
            @Param("languageCode") String languageCode,
            @Param("pharmacyId") Long pharmacyId,
            Pageable pageable);
    
    @Query("SELECT COUNT(pb) > 0 FROM PharmacyProductBarcode pb WHERE pb.barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);
    
    boolean existsByTypeId(Long typeId);
}
