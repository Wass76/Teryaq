package com.Teryaq.product.service;

import com.Teryaq.product.dto.*;
import com.Teryaq.product.entity.PurchaseInvoice;
import com.Teryaq.product.entity.PurchaseInvoiceItem;
import com.Teryaq.product.entity.PurchaseOrder;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.mapper.PurchaseInvoiceMapper;
import com.Teryaq.product.repo.PurchaseInvoiceRepo;
import com.Teryaq.product.repo.PurchaseInvoiceItemRepo;
import com.Teryaq.product.repo.PurchaseOrderRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.repository.SupplierRepository;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {
    private final PurchaseInvoiceRepo purchaseInvoiceRepo;
    private final PurchaseInvoiceItemRepo purchaseInvoiceItemRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final SupplierRepository supplierRepository;
    private final PurchaseInvoiceMapper purchaseInvoiceMapper;
    private final StockItemRepo stockItemRepo;
    private final MasterProductRepo masterProductRepo;

    @Transactional
    public PurchaseInvoiceDTOResponse create(PurchaseInvoiceDTORequest request) {
        PurchaseOrder order = purchaseOrderRepo.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        List<PurchaseInvoiceItem> items = request.getItems().stream().map(itemDto -> {
            if ("PHARMACY".equals(itemDto.getProductType())) {
                pharmacyProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + itemDto.getProductId()));
            } else if ("MASTER".equals(itemDto.getProductType())) {
                masterProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + itemDto.getProductId()));
            } else {
                throw new ConflictException("Invalid productType: " + itemDto.getProductType());
            }
            return purchaseInvoiceMapper.toItemEntity(itemDto);
        }).collect(Collectors.toList());
        if (items.isEmpty()) throw new ConflictException("Invoice must have at least one item");
        PurchaseInvoice invoice = purchaseInvoiceMapper.toEntity(request, supplier, items);
        invoice.setPurchaseOrder(order);
        invoice.setStatus("مكتمل");
        PurchaseInvoice saved = purchaseInvoiceRepo.save(invoice);
        // --- Stock Integration ---
        for (PurchaseInvoiceItem item : saved.getItems()) {
            StockItem stock = new StockItem();
            stock.setProductId(item.getProductId());
            stock.setProductType(item.getProductType());
            stock.setQuantity(item.getReceivedQty());
            stock.setBonusQty(item.getBonusQty());
            stock.setExpiryDate(item.getExpiryDate());
            stock.setBatchNo(item.getBatchNo());
            stock.setActualPurchasePrice(item.getActualPrice());
            stock.setPurchaseInvoice(saved);
            stock.setDateAdded(LocalDateTime.now());
            Object principal = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
            if (principal instanceof com.Teryaq.user.entity.User user) {
                stock.setAddedBy(user.getId());
            }
            stockItemRepo.save(stock);
        }
        // --- End Stock Integration ---
        order.setStatus("مكتمل");
        purchaseOrderRepo.save(order);
        // Fetch product lists for mapping
        List<Long> pharmacyProductIds = saved.getItems().stream()
            .filter(i -> "PHARMACY".equals(i.getProductType()))
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        List<Long> masterProductIds = saved.getItems().stream()
            .filter(i -> "MASTER".equals(i.getProductType()))
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(pharmacyProductIds);
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(masterProductIds);
        return purchaseInvoiceMapper.toResponse(saved, pharmacyProducts, masterProducts);
    }

    public PurchaseInvoiceDTOResponse getById(Long id) {
        PurchaseInvoice invoice = purchaseInvoiceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found"));
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> "PHARMACY".equals(i.getProductType()))
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> "MASTER".equals(i.getProductType()))
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        return purchaseInvoiceMapper.toResponse(invoice, pharmacyProducts, masterProducts);
    }

    public List<PurchaseInvoiceDTOResponse> listAll() {
        return purchaseInvoiceRepo.findAll().stream()
            .map(invoice -> {
                List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
                    invoice.getItems().stream()
                        .filter(i -> "PHARMACY".equals(i.getProductType()))
                        .map(PurchaseInvoiceItem::getProductId)
                        .toList()
                );
                List<MasterProduct> masterProducts = masterProductRepo.findAllById(
                    invoice.getItems().stream()
                        .filter(i -> "MASTER".equals(i.getProductType()))
                        .map(PurchaseInvoiceItem::getProductId)
                        .toList()
                );
                return purchaseInvoiceMapper.toResponse(invoice, pharmacyProducts, masterProducts);
            })
            .toList();
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseInvoice invoice = purchaseInvoiceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found"));
        if ("مكتمل".equals(invoice.getStatus())) {
            throw new ConflictException("Cannot cancel a completed invoice");
        }
        invoice.setStatus("ملغى");
        purchaseInvoiceRepo.save(invoice);
    }

    public PurchaseInvoice getInvoiceEntityById(Long id) {
        return purchaseInvoiceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found"));
    }
    public List<PurchaseInvoice> getAllInvoiceEntities() {
        return purchaseInvoiceRepo.findAll();
    }
    public PurchaseInvoiceMapper getMapper() {
        return purchaseInvoiceMapper;
    }
} 