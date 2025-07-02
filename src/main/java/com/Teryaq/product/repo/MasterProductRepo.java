package com.Teryaq.product.repo;

import com.Teryaq.product.entity.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterProductRepo extends JpaRepository<MasterProduct, Long> {
    @Query("""
    SELECT  p FROM MasterProduct p
    LEFT JOIN p.translations pt
    LEFT JOIN p.activeIngredients ai
    LEFT JOIN ai.translations ait
    WHERE (
        pt.language.code = :languageCode AND (
            LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    )
    OR (
        ait.language.code = :languageCode AND
        LOWER(ait.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<MasterProduct> search(@Param("keyword") String keyword,
                                            @Param("languageCode") String languageCode);

}
