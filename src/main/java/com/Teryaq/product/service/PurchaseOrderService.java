package com.Teryaq.product.service;

import com.Teryaq.product.dto.*;
import com.Teryaq.product.entity.PurchaseOrder;
import com.Teryaq.product.entity.PurchaseOrderItem;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.mapper.PurchaseOrderMapper;
import com.Teryaq.product.repo.PurchaseOrderRepo;
import com.Teryaq.product.repo.PurchaseOrderItemRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.repository.SupplierRepository;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final PurchaseOrderItemRepo purchaseOrderItemRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Transactional
    public PurchaseOrderDTOResponse create(PurchaseOrderDTORequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        List<PurchaseOrderItem> items = request.getItems().stream().map(itemDto -> {
            if ("PHARMACY".equals(itemDto.getProductType())) {
                pharmacyProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + itemDto.getProductId()));
            } else if ("MASTER".equals(itemDto.getProductType())) {
                masterProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + itemDto.getProductId()));
            } else {
                throw new ConflictException("Invalid productType: " + itemDto.getProductType());
            }
            return purchaseOrderMapper.toItemEntity(itemDto);
        }).collect(Collectors.toList());
        if (items.isEmpty()) throw new ConflictException("Order must have at least one item");
        PurchaseOrder order = purchaseOrderMapper.toEntity(request, supplier, items);
        PurchaseOrder saved = purchaseOrderRepo.save(order);
        // Fetch product lists for mapping
        List<Long> pharmacyProductIds = saved.getItems().stream()
            .filter(i -> "PHARMACY".equals(i.getProductType()))
            .map(PurchaseOrderItem::getProductId)
            .toList();
        List<Long> masterProductIds = saved.getItems().stream()
            .filter(i -> "MASTER".equals(i.getProductType()))
            .map(PurchaseOrderItem::getProductId)
            .toList();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(pharmacyProductIds);
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(masterProductIds);
        return purchaseOrderMapper.toResponse(saved, pharmacyProducts, masterProducts);
    }

    public PurchaseOrderDTOResponse getById(Long id) {
        PurchaseOrder order = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        // Fetch product names for all items
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> "PHARMACY".equals(i.getProductType()))
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> "MASTER".equals(i.getProductType()))
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        // Merge names for mapping
        return purchaseOrderMapper.toResponse(order, pharmacyProducts, masterProducts);
    }

    public List<PurchaseOrderDTOResponse> listAll() {
        return purchaseOrderRepo.findAll().stream()
            .map(order -> {
                List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
                    order.getItems().stream()
                        .filter(i -> "PHARMACY".equals(i.getProductType()))
                        .map(PurchaseOrderItem::getProductId)
                        .toList()
                );
                List<MasterProduct> masterProducts = masterProductRepo.findAllById(
                    order.getItems().stream()
                        .filter(i -> "MASTER".equals(i.getProductType()))
                        .map(PurchaseOrderItem::getProductId)
                        .toList()
                );
                return purchaseOrderMapper.toResponse(order, pharmacyProducts, masterProducts);
            })
            .toList();
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder order = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if ("مكتمل".equals(order.getStatus())) {
            throw new ConflictException("Cannot cancel a completed order");
        }
        order.setStatus("ملغى");
        purchaseOrderRepo.save(order);
    }

    public PurchaseOrder getOrderEntityById(Long id) {
        return purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
    }
    public List<PurchaseOrder> getAllOrderEntities() {
        return purchaseOrderRepo.findAll();
    }
    public PurchaseOrderMapper getMapper() {
        return purchaseOrderMapper;
    }
} 