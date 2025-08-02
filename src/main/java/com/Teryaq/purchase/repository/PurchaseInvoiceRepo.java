package com.Teryaq.purchase.repository;

import com.Teryaq.purchase.entity.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceRepo extends JpaRepository<PurchaseInvoice, Long> {
    
    /**
     * Find all purchase invoices by pharmacy ID
     */
    List<PurchaseInvoice> findByPharmacyId(Long pharmacyId);
    
    /**
     * Find all purchase invoices by pharmacy ID with pagination
     */
    Page<PurchaseInvoice> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find purchase invoice by ID and pharmacy ID
     */
    java.util.Optional<PurchaseInvoice> findByIdAndPharmacyId(Long id, Long pharmacyId);
    

} 