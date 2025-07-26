package com.Teryaq.product.service;

import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.product.Enum.ProductType;
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
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    public PurchaseOrderDTOResponse create(PurchaseOrderDTORequest request, String language) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        List<PurchaseOrderItem> items = request.getItems().stream().map(itemDto -> {
            String barcode = itemDto.getBarcode();
            Double price = itemDto.getPrice();
            if (itemDto.getProductType() == ProductType.PHARMACY) {
                PharmacyProduct product = pharmacyProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + itemDto.getProductId()));
                if (barcode == null || barcode.isBlank()) {
                    barcode = product.getBarcodes().stream().findFirst().map(b -> b.getBarcode()).orElse(null);
                }
                if (price == null) {
                    price = (double) product.getRefPurchasePrice();
                }
            } else if (itemDto.getProductType() == ProductType.MASTER) {
                MasterProduct product = masterProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + itemDto.getProductId()));
                if (barcode == null || barcode.isBlank()) {
                    barcode = product.getBarcode();
                }
                // Always use master product price
                price = (double) product.getRefPurchasePrice();
            } else {
                throw new ConflictException("Invalid productType: " + itemDto.getProductType());
            }
            return purchaseOrderMapper.toItemEntity(itemDto, barcode, price);
        }).collect(Collectors.toList());
        if (items.isEmpty()) throw new ConflictException("Order must have at least one item");
        PurchaseOrder order = purchaseOrderMapper.toEntity(request, supplier, items);
        PurchaseOrder saved = purchaseOrderRepo.save(order);
        // Fetch product lists for mapping
        List<Long> pharmacyProductIds = saved.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .toList();
        List<Long> masterProductIds = saved.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .toList();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(pharmacyProductIds);
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(masterProductIds);
        return purchaseOrderMapper.toResponse(saved, pharmacyProducts, masterProducts, language);
    }

    public PurchaseOrderDTOResponse create(PurchaseOrderDTORequest request) {
        return create(request, "ar");
    }

    public PurchaseOrderDTOResponse getById(Long id, String language) {
        PurchaseOrder order = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        // Fetch product names for all items
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> i.getProductType() == ProductType.PHARMACY)
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(
            order.getItems().stream()
                .filter(i -> i.getProductType() == ProductType.MASTER)
                .map(PurchaseOrderItem::getProductId)
                .toList()
        );
        // Merge names for mapping
        return purchaseOrderMapper.toResponse(order, pharmacyProducts, masterProducts, language);
    }

    public PurchaseOrderDTOResponse getById(Long id) {
        return getById(id, "ar");
    }

    public List<PurchaseOrderDTOResponse> listAll(String language) {
        List<PurchaseOrder> orders = purchaseOrderRepo.findAll();
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        return orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
    }

    public PaginationDTO<PurchaseOrderDTOResponse> listAllPaginated(int page, int size, String language) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findAll(pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> listAllPaginated(int page, int size) {
        return listAllPaginated(page, size, "ar");
    }

    public List<PurchaseOrderDTOResponse> getByStatus(OrderStatus status, String language) {
        List<PurchaseOrder> orders = purchaseOrderRepo.findByStatus(status);
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        return orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
    }

    public List<PurchaseOrderDTOResponse> getByStatus(OrderStatus status) {
        return getByStatus(status, "ar");
    }

    public PaginationDTO<PurchaseOrderDTOResponse> getByStatusPaginated(OrderStatus status, int page, int size, String language) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findByStatus(status, pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> getByStatusPaginated(OrderStatus status, int page, int size) {
        return getByStatusPaginated(status, page, size, "ar");
    }

    public List<PurchaseOrderDTOResponse> listAll() {
        return listAll("ar");
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder order = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (order.getStatus() == OrderStatus.DONE) {
            throw new ConflictException("Cannot cancel a completed order");
        }
        order.setStatus(OrderStatus.CANCELLED);
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