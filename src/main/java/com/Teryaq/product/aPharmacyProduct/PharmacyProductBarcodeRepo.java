package com.Teryaq.product.aPharmacyProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyProductBarcodeRepo extends JpaRepository<PharmacyProductBarcode, Long> {
    
    Optional<PharmacyProductBarcode> findByBarcode(String barcode);
    
    boolean existsByBarcode(String barcode);
    
    List<PharmacyProductBarcode> findByProductId(Long productId);
    
    @Query("SELECT pb FROM PharmacyProductBarcode pb WHERE pb.barcode IN :barcodes")
    List<PharmacyProductBarcode> findByBarcodes(@Param("barcodes") List<String> barcodes);
} 