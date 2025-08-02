package com.Teryaq.sale.repo;

import com.Teryaq.sale.entity.SaleInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, Long> {
    
    /**
     * Find all sale invoices by pharmacy ID
     */
    List<SaleInvoice> findByPharmacyId(Long pharmacyId);
    
    /**
     * Find all sale invoices by pharmacy ID with pagination
     */
    Page<SaleInvoice> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find sale invoice by ID and pharmacy ID
     */
    Optional<SaleInvoice> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
    /**
     * Check if sale invoice exists by ID and pharmacy ID
     */

}
