package com.Teryaq.purchase.service;

import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.*;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.purchase.entity.PurchaseInvoice;
import com.Teryaq.purchase.entity.PurchaseInvoiceItem;
import com.Teryaq.purchase.entity.PurchaseOrder;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.purchase.mapper.PurchaseInvoiceMapper;
import com.Teryaq.purchase.repository.PurchaseInvoiceRepo;
import com.Teryaq.purchase.repository.PurchaseInvoiceItemRepo;
import com.Teryaq.purchase.repository.PurchaseOrderRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.repository.SupplierRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService extends BaseSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseInvoiceService.class);
    private final PurchaseInvoiceRepo purchaseInvoiceRepo;
    private final PurchaseInvoiceItemRepo purchaseInvoiceItemRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final SupplierRepository supplierRepository;
    private final PurchaseInvoiceMapper purchaseInvoiceMapper;
    private final StockItemRepo stockItemRepo;
    private final MasterProductRepo masterProductRepo;

    public PurchaseInvoiceService(PurchaseInvoiceRepo purchaseInvoiceRepo,
                                PurchaseInvoiceItemRepo purchaseInvoiceItemRepo,
                                PurchaseOrderRepo purchaseOrderRepo,
                                PharmacyProductRepo pharmacyProductRepo,
                                SupplierRepository supplierRepository,
                                PurchaseInvoiceMapper purchaseInvoiceMapper,
                                StockItemRepo stockItemRepo,
                                MasterProductRepo masterProductRepo,
                                com.Teryaq.user.repository.UserRepository userRepository) {
        super(userRepository);
        this.purchaseInvoiceRepo = purchaseInvoiceRepo;
        this.purchaseInvoiceItemRepo = purchaseInvoiceItemRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.supplierRepository = supplierRepository;
        this.purchaseInvoiceMapper = purchaseInvoiceMapper;
        this.stockItemRepo = stockItemRepo;
        this.masterProductRepo = masterProductRepo;
    }

    @Transactional
    public PurchaseInvoiceDTOResponse create(PurchaseInvoiceDTORequest request, String language) {
        logger.info("Creating purchase invoice for pharmacy: {}", getCurrentUserPharmacyId());
        // Get current user's pharmacy
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        
        PurchaseOrder order = purchaseOrderRepo.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        
        // Validate that the order belongs to the current user's pharmacy
        validatePharmacyAccess(order.getPharmacy());
        
        // Only allow invoice creation if order is PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Cannot create invoice for order that is not PENDING. Current status: " + order.getStatus());
        }
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        
        List<PurchaseInvoiceItem> items = request.getItems().stream().map(itemDto -> {
            if (itemDto.getProductType() == ProductType.PHARMACY) {
                pharmacyProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + itemDto.getProductId()));
            } else if (itemDto.getProductType() == ProductType.MASTER) {
                masterProductRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + itemDto.getProductId()));
            } else {
                throw new ConflictException("Invalid productType: " + itemDto.getProductType());
            }
            return purchaseInvoiceMapper.toItemEntity(itemDto);
        }).collect(Collectors.toList());
        
        if (items.isEmpty())
            throw new ConflictException("Invoice must have at least one item");
        PurchaseInvoice invoice = purchaseInvoiceMapper.toEntity(request, supplier, items);
        invoice.setPurchaseOrder(order);
        invoice.setPharmacy(currentPharmacy); // Set the pharmacy for the invoice
        
        // Set actualPrice for each item (use invoicePrice as the actual price paid)
        invoice.getItems().forEach(item -> {
            item.setActualPrice(item.getInvoicePrice());
        });
        
        // Calculate total as sum of invoicePrice * receivedQty for each item
        double total = invoice.getItems().stream()
            .mapToDouble(item -> (item.getInvoicePrice() != null ? item.getInvoicePrice() : 0.0) * (item.getReceivedQty() != null ? item.getReceivedQty() : 0))
            .sum();
        invoice.setTotal(total);
        
        // Save the invoice first
        PurchaseInvoice saved = purchaseInvoiceRepo.save(invoice);
        
        // Explicitly save each item to ensure they are persisted
        for (PurchaseInvoiceItem item : saved.getItems()) {
            item.setPurchaseInvoice(saved);
            purchaseInvoiceItemRepo.save(item);
        }
        
        // Refresh the saved invoice to get the updated items
        saved = purchaseInvoiceRepo.findById(saved.getId()).orElse(saved);
        
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
            stock.setDateAdded(LocalDate.now());
            Object principal = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
            if (principal instanceof com.Teryaq.user.entity.User user) {
                stock.setAddedBy(user.getId());
            }
            stockItemRepo.save(stock);
        }
        // --- End Stock Integration ---
        order.setStatus(OrderStatus.DONE);
        purchaseOrderRepo.save(order);
        // Fetch product lists for mapping
        List<Long> pharmacyProductIds = saved.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.PHARMACY)
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        List<Long> masterProductIds = saved.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.MASTER)
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(pharmacyProductIds);
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(masterProductIds);
        return purchaseInvoiceMapper.toResponse(saved, pharmacyProducts, masterProducts, language);
    }

    public PurchaseInvoiceDTOResponse create(PurchaseInvoiceDTORequest request) {
        return create(request, "ar");
    }

    public PurchaseInvoiceDTOResponse getById(Long id, String language) {
        // Get current user's pharmacy ID
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // Find invoice by ID and pharmacy ID to ensure pharmacy isolation
        PurchaseInvoice invoice = purchaseInvoiceRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found with ID: " + id));
        
        // Fetch product names for all items
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> i.getProductType() == ProductType.PHARMACY)
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        List<MasterProduct> masterProducts = masterProductRepo.findAllById(
            invoice.getItems().stream()
                .filter(i -> i.getProductType() == ProductType.MASTER)
                .map(PurchaseInvoiceItem::getProductId)
                .toList()
        );
        // Merge names for mapping
        return purchaseInvoiceMapper.toResponse(invoice, pharmacyProducts, masterProducts, language);
    }

    public PurchaseInvoiceDTOResponse getById(Long id) {
        return getById(id, "ar");
    }

    public List<PurchaseInvoiceDTOResponse> listAll(String language) {
        // Get current user's pharmacy ID
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // Find all invoices for the current pharmacy
        List<PurchaseInvoice> invoices = purchaseInvoiceRepo.findByPharmacyId(currentPharmacyId);
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        return invoices.stream()
            .map(invoice -> purchaseInvoiceMapper.toResponse(invoice, allPharmacyProducts, allMasterProducts, language))
            .toList();
    }

    public List<PurchaseInvoiceDTOResponse> listAll() {
        return listAll("ar");
    }

    public PaginationDTO<PurchaseInvoiceDTOResponse> listAllPaginated(int page, int size, String language) {
        // Get current user's pharmacy ID
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseInvoice> invoicePage = purchaseInvoiceRepo.findByPharmacyId(currentPharmacyId, pageable);
        
        List<PurchaseInvoice> invoices = invoicePage.getContent();
        
        // Collect all product IDs efficiently
        List<Long> allPharmacyProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
            
        List<Long> allMasterProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
        
        // Fetch all products once
        List<PharmacyProduct> allPharmacyProducts = pharmacyProductRepo.findAllById(allPharmacyProductIds);
        List<MasterProduct> allMasterProducts = masterProductRepo.findAllById(allMasterProductIds);
        
        List<PurchaseInvoiceDTOResponse> responses = invoices.stream()
            .map(invoice -> purchaseInvoiceMapper.toResponse(invoice, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, invoicePage.getTotalElements());
    }

    public PaginationDTO<PurchaseInvoiceDTOResponse> listAllPaginated(int page, int size) {
        return listAllPaginated(page, size, "ar");
    }

//    @Transactional
//    public void cancel(Long id) {
//        PurchaseInvoice invoice = purchaseInvoiceRepo.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found"));
//        if ("مكتمل".equals(invoice.getStatus())) {
//            throw new ConflictException("Cannot cancel a completed invoice");
//        }
//        invoice.setStatus("ملغى");
//        purchaseInvoiceRepo.save(invoice);
//    }


} 