package com.Teryaq.purchase.service;

import com.Teryaq.moneybox.enums.TransactionType;
import com.Teryaq.moneybox.repository.MoneyBoxRepository;
import com.Teryaq.product.Enum.OrderStatus;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.*;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTORequest;
import com.Teryaq.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Teryaq.purchase.dto.PurchaseInvoiceItemDTORequest;
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
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.Supplier;
import com.Teryaq.user.repository.SupplierRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import com.Teryaq.moneybox.service.PurchaseIntegrationService;
import com.Teryaq.moneybox.service.ExchangeRateService;
import com.Teryaq.moneybox.service.EnhancedMoneyBoxAuditService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import com.Teryaq.utils.exception.RequestNotValidException;
import java.util.Arrays;

@Service
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
    private final PurchaseIntegrationService purchaseIntegrationService;
    private final ExchangeRateService exchangeRateService;
    private final EnhancedMoneyBoxAuditService enhancedAuditService;
    private final MoneyBoxRepository moneyBoxRepository;

    public PurchaseInvoiceService(PurchaseInvoiceRepo purchaseInvoiceRepo,
                                  PurchaseInvoiceItemRepo purchaseInvoiceItemRepo,
                                  PurchaseOrderRepo purchaseOrderRepo,
                                  PharmacyProductRepo pharmacyProductRepo,
                                  SupplierRepository supplierRepository,
                                  PurchaseInvoiceMapper purchaseInvoiceMapper,
                                  StockItemRepo stockItemRepo,
                                  MasterProductRepo masterProductRepo,
                                  PurchaseIntegrationService purchaseIntegrationService,
                                  ExchangeRateService exchangeRateService,
                                  EnhancedMoneyBoxAuditService enhancedAuditService,
                                  com.Teryaq.user.repository.UserRepository userRepository, MoneyBoxRepository moneyBoxRepository) {
        super(userRepository);
        this.purchaseInvoiceRepo = purchaseInvoiceRepo;
        this.purchaseInvoiceItemRepo = purchaseInvoiceItemRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.supplierRepository = supplierRepository;
        this.purchaseInvoiceMapper = purchaseInvoiceMapper;
        this.stockItemRepo = stockItemRepo;
        this.masterProductRepo = masterProductRepo;
        this.purchaseIntegrationService = purchaseIntegrationService;
        this.exchangeRateService = exchangeRateService;
        this.enhancedAuditService = enhancedAuditService;
        this.moneyBoxRepository = moneyBoxRepository;
    }

    @Transactional
    public PurchaseInvoiceDTOResponse create(PurchaseInvoiceDTORequest request, String language) {
        logger.info("Creating purchase invoice for pharmacy: {}", getCurrentUserPharmacyId());
        
        // Validate and get required entities
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        PurchaseOrder order = validateAndGetPurchaseOrder(request);
        Supplier supplier = getSupplier(request.getSupplierId());
        List<PurchaseInvoiceItem> items = validateAndCreateInvoiceItems(request);
        
        // Create and save invoice
        PurchaseInvoice invoice = createAndSaveInvoice(request, supplier, items, order, currentPharmacy);
        
        // Update order status
        updateOrderStatus(order);
        
        // Create StockItem records for audit trail
        createStockItemRecords(invoice, request);
        
        // Get products for response mapping
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(invoice);
        List<MasterProduct> masterProducts = getMasterProducts(invoice);
        
        return purchaseInvoiceMapper.toResponse(invoice, pharmacyProducts, masterProducts, language);
    }

    public PurchaseInvoiceDTOResponse create(PurchaseInvoiceDTORequest request) {
        return create(request, "ar");
    }

    public PurchaseInvoiceDTOResponse getById(Long id, String language) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        PurchaseInvoice invoice = getInvoiceByIdAndPharmacy(id, currentPharmacyId);
        
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(invoice);
        List<MasterProduct> masterProducts = getMasterProducts(invoice);
        
        return purchaseInvoiceMapper.toResponse(invoice, pharmacyProducts, masterProducts, language);
    }

    public PurchaseInvoiceDTOResponse getById(Long id) {
        return getById(id, "ar");
    }

    @Transactional
    public PurchaseInvoiceDTOResponse edit(Long id, PurchaseInvoiceDTORequest request, String language) {
        logger.info("Editing purchase invoice with ID: {} for pharmacy: {}", id, getCurrentUserPharmacyId());
        
        // ✅ ADD CURRENCY VALIDATION
        if (request.getCurrency() == null) {
            request.setCurrency(Currency.SYP);
            logger.info("Currency not specified, defaulting to SYP for edited purchase invoice");
        }
        
        // Validate currency is supported
        if (!Arrays.asList(Currency.SYP, Currency.USD, Currency.EUR).contains(request.getCurrency())) {
            throw new RequestNotValidException("Unsupported currency: " + request.getCurrency() + 
                ". Supported currencies are: SYP, USD, EUR");
        }
        
        // Validate and get required entities
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        PurchaseInvoice invoice = getInvoiceByIdAndPharmacy(id, currentPharmacy.getId());
        validateInvoiceCanBeEdited(invoice);
        
        PurchaseOrder order = validateAndGetPurchaseOrder(request);
        Supplier supplier = getSupplier(request.getSupplierId());
        List<PurchaseInvoiceItem> items = validateAndCreateInvoiceItems(request);
        
        // Set the purchaseInvoice reference on each new item
        items.forEach(item -> item.setPurchaseInvoice(invoice));
        
        // Update invoice properties
        invoice.setPurchaseOrder(order);
        invoice.setSupplier(supplier);
        invoice.setCurrency(request.getCurrency());
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        
        // Properly manage the items collection to avoid Hibernate cascade issues
        invoice.getItems().clear();
        invoice.getItems().addAll(items);
        
        // Convert prices to SYP and recalculate totals
        setInvoiceItemPrices(invoice);
        calculateInvoiceTotal(invoice);
        
        PurchaseInvoice saved = purchaseInvoiceRepo.save(invoice);
        
        // Integrate with Money Box for cash payments (if payment method changed to cash)
        if (request.getPaymentMethod() == com.Teryaq.product.Enum.PaymentMethod.CASH) {
            try {
                // Get current pharmacy ID for MoneyBox integration
                Long currentPharmacyId = getCurrentUserPharmacyId();
                purchaseIntegrationService.recordPurchasePayment(
                    currentPharmacyId,
                    saved.getId(),
                    BigDecimal.valueOf(saved.getTotal()),
                    request.getCurrency()
                );
                logger.info("Cash purchase recorded in Money Box for edited invoice: {}", saved.getId());
            } catch (Exception e) {
                logger.warn("Failed to record cash purchase in Money Box for edited invoice {}: {}", 
                           saved.getId(), e.getMessage());
                // Don't fail the edit if Money Box integration fails
            }
        }
        
        // Get products for response mapping
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(saved);
        List<MasterProduct> masterProducts = getMasterProducts(saved);
        
        return purchaseInvoiceMapper.toResponse(saved, pharmacyProducts, masterProducts, language);
    }

    public PurchaseInvoiceDTOResponse edit(Long id, PurchaseInvoiceDTORequest request) {
        return edit(id, request, "ar");
    }

    private void validateInvoiceCanBeEdited(PurchaseInvoice invoice) {
        // Check if the associated order is in a state that allows editing
        if (invoice.getPurchaseOrder().getStatus() == OrderStatus.DONE) {
            throw new ConflictException("Cannot edit invoice for a completed order");
        }
    }
    



    public PaginationDTO<PurchaseInvoiceDTOResponse> listAllPaginated(int page, int size, String language) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseInvoice> invoicePage = purchaseInvoiceRepo.findByPharmacyId(currentPharmacyId, pageable);
        
        List<PurchaseInvoice> invoices = invoicePage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(invoices);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(invoices);
        
        List<PurchaseInvoiceDTOResponse> responses = invoices.stream()
            .map(invoice -> purchaseInvoiceMapper.toResponse(invoice, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, invoicePage.getTotalElements());
    }

    public PaginationDTO<PurchaseInvoiceDTOResponse> listAllPaginated(int page, int size) {
        return listAllPaginated(page, size, "ar");
    }

    // New method for filtering by time range
    public PaginationDTO<PurchaseInvoiceDTOResponse> getByTimeRangePaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, String language) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseInvoice> invoicePage = purchaseInvoiceRepo.findByPharmacyIdAndCreatedAtBetween(
            currentPharmacyId, startDate, endDate, pageable);
        
        List<PurchaseInvoice> invoices = invoicePage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(invoices);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(invoices);
        
        List<PurchaseInvoiceDTOResponse> responses = invoices.stream()
            .map(invoice -> purchaseInvoiceMapper.toResponse(invoice, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, invoicePage.getTotalElements());
    }

    public PaginationDTO<PurchaseInvoiceDTOResponse> getByTimeRangePaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return getByTimeRangePaginated(startDate, endDate, page, size, "ar");
    }

    // New method for filtering by supplier
    public PaginationDTO<PurchaseInvoiceDTOResponse> getBySupplierPaginated(
            Long supplierId, int page, int size, String language) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // Validate supplier exists
        getSupplier(supplierId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseInvoice> invoicePage = purchaseInvoiceRepo.findByPharmacyIdAndSupplierId(
            currentPharmacyId, supplierId, pageable);
        
        List<PurchaseInvoice> invoices = invoicePage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(invoices);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(invoices);
        
        List<PurchaseInvoiceDTOResponse> responses = invoices.stream()
            .map(invoice -> purchaseInvoiceMapper.toResponse(invoice, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, invoicePage.getTotalElements());
    }

    public PaginationDTO<PurchaseInvoiceDTOResponse> getBySupplierPaginated(
            Long supplierId, int page, int size) {
        return getBySupplierPaginated(supplierId, page, size, "ar");
    }

    // Private helper methods for invoice creation
    private PurchaseOrder validateAndGetPurchaseOrder(PurchaseInvoiceDTORequest request) {
        PurchaseOrder order = purchaseOrderRepo.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        
        validatePharmacyAccess(order.getPharmacy());
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Cannot create invoice for order that is not PENDING. Current status: " + order.getStatus());
        }
        
        return order;
    }

    private Supplier getSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private List<PurchaseInvoiceItem> validateAndCreateInvoiceItems(PurchaseInvoiceDTORequest request) {
        List<PurchaseInvoiceItem> items = request.getItems().stream()
            .map(this::validateAndCreateInvoiceItem)
            .collect(Collectors.toList());
        
        if (items.isEmpty()) {
            throw new ConflictException("Invoice must have at least one item");
        }
        
        return items;
    }

    private PurchaseInvoiceItem validateAndCreateInvoiceItem(PurchaseInvoiceItemDTORequest itemDto) {
        if (itemDto.getProductType() == ProductType.PHARMACY) {
            // Validate that sellingPrice is provided for PHARMACY products
            if (itemDto.getSellingPrice() == null || itemDto.getSellingPrice() <= 0) {
                throw new ConflictException("Selling price is required and must be greater than 0 for PHARMACY type products");
            }
            
            pharmacyProductRepo.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + itemDto.getProductId()));
        } else if (itemDto.getProductType() == ProductType.MASTER) {
            // For MASTER products, sellingPrice is optional and will not be set
            masterProductRepo.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + itemDto.getProductId()));
        } else {
            throw new ConflictException("Invalid productType: " + itemDto.getProductType());
        }
        
        return purchaseInvoiceMapper.toItemEntity(itemDto);
    }

    private PurchaseInvoice createAndSaveInvoice(PurchaseInvoiceDTORequest request, Supplier supplier, 
                                               List<PurchaseInvoiceItem> items, PurchaseOrder order, Pharmacy currentPharmacy) {
        PurchaseInvoice invoice = purchaseInvoiceMapper.toEntity(request, supplier, items);
        invoice.setPurchaseOrder(order);
        invoice.setPharmacy(currentPharmacy);
        
        setInvoiceItemPrices(invoice);
        calculateInvoiceTotal(invoice);
        
        PurchaseInvoice saved = purchaseInvoiceRepo.save(invoice);
        saveInvoiceItems(saved);
        
            // Record financial audit trail using enhanced MoneyBox audit
            // Only record for non-cash payments (cash payments are handled by purchaseIntegrationService)
            if (request.getPaymentMethod() != com.Teryaq.product.Enum.PaymentMethod.CASH) {
                try {
                    // Get MoneyBox ID for the current pharmacy
                    Long moneyBoxId = getMoneyBoxIdForPharmacy(currentPharmacy.getId());
                    
                    enhancedAuditService.recordFinancialOperation(
                        moneyBoxId,
                        com.Teryaq.moneybox.enums.TransactionType.PURCHASE_PAYMENT,
                        BigDecimal.valueOf(saved.getTotal()),
                        request.getCurrency(),
                        "Purchase invoice created for supplier: " + supplier.getName(),
                        String.valueOf(saved.getId()),
                        "PURCHASE_INVOICE",
                        getCurrentUser().getId(),
                        getCurrentUser().getClass().getSimpleName(),
                        null, // IP address - would need to be passed from controller
                        null, // User agent - would need to be passed from controller
                        null, // Session ID - would need to be passed from controller
                        Map.of("supplierId", supplier.getId(), "paymentMethod", request.getPaymentMethod().name())
                    );
                } catch (Exception e) {
                    logger.warn("Failed to record enhanced audit trail for invoice {}: {}", saved.getId(), e.getMessage());
                }
            }
        
        // Integrate with Money Box for cash payments
        if (request.getPaymentMethod() == com.Teryaq.product.Enum.PaymentMethod.CASH) {
            try {
                // Get current pharmacy ID for MoneyBox integration
                Long currentPharmacyId = getCurrentUserPharmacyId();
                purchaseIntegrationService.recordPurchasePayment(
                    currentPharmacyId,
                    saved.getId(),
                    BigDecimal.valueOf(saved.getTotal()),
                    request.getCurrency()
                );
                // Note: purchaseIntegrationService.recordPurchasePayment() already creates the transaction
                // No need for additional enhancedAuditService.recordFinancialOperation() call
                
                logger.info("Cash purchase recorded in Money Box for invoice: {}", saved.getId());
            } catch (Exception e) {
                logger.warn("Failed to record cash purchase in Money Box for invoice {}: {}", 
                           saved.getId(), e.getMessage());
                // Don't fail the purchase if Money Box integration fails
            }
        }
        
        return purchaseInvoiceRepo.findById(saved.getId()).orElse(saved);
    }

    private void setInvoiceItemPrices(PurchaseInvoice invoice) {
        Currency invoiceCurrency = invoice.getCurrency();
        
        invoice.getItems().forEach(item -> {
            // Convert invoice price from request currency to SYP before storing
            Double invoicePriceInSYP = convertPriceToSYP(item.getInvoicePrice(), invoiceCurrency);
            item.setInvoicePrice(invoicePriceInSYP);
            
            // Calculate actual price after bonus (now in SYP)
            Double actualPrice = calculateActualPurchasePrice(item);
            logger.info("The actual price of the item {} from type {} is {} SYP (converted from {} {})" , 
                item.getProductId(), item.getProductType(), actualPrice, item.getInvoicePrice(), invoiceCurrency);
            item.setActualPrice(actualPrice);
        });
    }

    private void calculateInvoiceTotal(PurchaseInvoice invoice) {
        // Calculate total in SYP (since all prices are now stored in SYP)
        double totalInSYP = invoice.getItems().stream()
            .mapToDouble(item -> (item.getInvoicePrice() != null ? item.getInvoicePrice() : 0.0) * 
                                (item.getReceivedQty() != null ? item.getReceivedQty() : 0))
            .sum();
        
        // Convert total from SYP to the invoice currency for storage
        Currency invoiceCurrency = invoice.getCurrency();
        Double totalInRequestedCurrency = convertPriceFromSYP(totalInSYP, invoiceCurrency);
        
        logger.info("The total price of the invoice {} is {} {} (calculated from {} SYP)", 
            invoice.getId(), totalInRequestedCurrency, invoiceCurrency, totalInSYP);
        invoice.setTotal(totalInRequestedCurrency);
    }

    private void saveInvoiceItems(PurchaseInvoice invoice) {
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            item.setPurchaseInvoice(invoice);
            purchaseInvoiceItemRepo.save(item);
        }
    }

    private void updateOrderStatus(PurchaseOrder order) {
        order.setStatus(OrderStatus.DONE);
        purchaseOrderRepo.save(order);
    }

    // Method to create StockItem records for audit trail
    private void createStockItemRecords(PurchaseInvoice invoice, PurchaseInvoiceDTORequest request) {
        for (PurchaseInvoiceItem item : invoice.getItems()) {
            // Validate expiry date
            validateExpiryDate(item.getExpiryDate());
            
            // Calculate actual purchase price after bonus
            Double actualPurchasePrice = calculateActualPurchasePrice(item);
            
            // Update the actualPrice in the PurchaseInvoiceItem
            item.setActualPrice(actualPurchasePrice);
            
            StockItem stockItem = new StockItem();
            
            // Set basic properties
            stockItem.setProductId(item.getProductId());
            stockItem.setProductType(item.getProductType());
            stockItem.setQuantity(item.getReceivedQty());
            stockItem.setBonusQty(item.getBonusQty());
            stockItem.setActualPurchasePrice(actualPurchasePrice);
            
            // Set minStockLevel from request
            PurchaseInvoiceItemDTORequest requestItem = request.getItems().stream()
                .filter(reqItem -> reqItem.getProductId().equals(item.getProductId()) && 
                                 reqItem.getProductType() == item.getProductType())
                .findFirst()
                .orElse(null);
            
            if (requestItem != null && requestItem.getMinStockLevel() != null) {
                stockItem.setMinStockLevel(requestItem.getMinStockLevel());
            }
            
            // Set batch and invoice details
            stockItem.setBatchNo(item.getBatchNo());
            stockItem.setInvoiceNumber(item.getInvoiceNumber());
            stockItem.setExpiryDate(item.getExpiryDate());
            
            // Link to purchase invoice
            stockItem.setPurchaseInvoice(invoice);
            
            // Set pharmacy
            stockItem.setPharmacy(invoice.getPharmacy());
            
            // Set timestamps
            stockItem.setDateAdded(LocalDate.now());
            stockItem.setAddedBy(getCurrentUser().getId());
            
            // Save the stock item
            stockItemRepo.save(stockItem);
            
            // Update product information if changed (prices, minStockLevel)
            updateProductInformationIfChanged(item, actualPurchasePrice, request);
        }
        
        // Save the updated PurchaseInvoiceItem entities
        purchaseInvoiceItemRepo.saveAll(invoice.getItems());
    }

    // Validate expiry date
    private void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new ConflictException("Expiry date is required for all items");
        }
        
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            throw new ConflictException("Cannot accept items with expired date: " + expiryDate);
        }
        
        // Warning for items expiring within 6 months
        LocalDate sixMonthsFromNow = today.plusMonths(6);
        if (expiryDate.isBefore(sixMonthsFromNow)) {
            logger.warn("Item with expiry date {} is less than 6 months from now", expiryDate);
            // You can add user notification here if needed
        }
    }

    // Calculate actual purchase price after bonus
    private Double calculateActualPurchasePrice(PurchaseInvoiceItem item) {
        if (item.getReceivedQty() == null || item.getReceivedQty() <= 0) {
            throw new ConflictException("Received quantity must be greater than 0");
        }
        
        if (item.getInvoicePrice() == null || item.getInvoicePrice() <= 0) {
            throw new ConflictException("Invoice price must be greater than 0");
        }
        
        int bonusQty = item.getBonusQty() != null ? item.getBonusQty() : 0;
        int totalQty = item.getReceivedQty() + bonusQty;
        
        if (totalQty <= 0) {
            throw new ConflictException("Total quantity (received + bonus) must be greater than 0");
        }
        
        // Formula: (receivedQty × invoicePrice) ÷ (receivedQty + bonusQty)
        return (item.getReceivedQty() * item.getInvoicePrice()) / totalQty;
    }

    // Update product information if changed (prices, minStockLevel)
    private void updateProductInformationIfChanged(PurchaseInvoiceItem item, Double actualPurchasePrice, PurchaseInvoiceDTORequest request) {
        // Find the corresponding item in the request to get additional information
        PurchaseInvoiceItemDTORequest requestItem = request.getItems().stream()
            .filter(reqItem -> reqItem.getProductId().equals(item.getProductId()) && 
                             reqItem.getProductType() == item.getProductType())
            .findFirst()
            .orElse(null);
        
        if (requestItem == null) return;
        
        if (item.getProductType() == ProductType.PHARMACY) {
            updatePharmacyProductInformation(item, actualPurchasePrice, requestItem);
        } else if (item.getProductType() == ProductType.MASTER) {
            updateMasterProductInformation(item, requestItem);
        }
    }
    
    private void updatePharmacyProductInformation(PurchaseInvoiceItem item, Double actualPurchasePrice, PurchaseInvoiceItemDTORequest requestItem) {
        PharmacyProduct product = pharmacyProductRepo.findById(item.getProductId()).orElse(null);
        if (product == null) return;
        
        boolean updated = false;
        
        // Update refPurchasePrice if it's different
        if (actualPurchasePrice != null && !actualPurchasePrice.equals(product.getRefPurchasePrice())) {
            product.setRefPurchasePrice(actualPurchasePrice.floatValue());
            updated = true;
            logger.info("Updated refPurchasePrice for PharmacyProduct {} from {} to {}", 
                product.getId(), product.getRefPurchasePrice(), actualPurchasePrice);
        }
        
        // Update refSellingPrice if provided in the request
        if (requestItem.getSellingPrice() != null) {
            // Convert selling price from request currency to SYP before storing
            Currency requestCurrency = item.getPurchaseInvoice().getCurrency();
            Double sellingPriceInSYP = convertSellingPriceToSYP(requestItem.getSellingPrice(), requestCurrency);
            
            if (!sellingPriceInSYP.equals(product.getRefSellingPrice())) {
                product.setRefSellingPrice(sellingPriceInSYP.floatValue());
                updated = true;
                logger.info("Updated refSellingPrice for PharmacyProduct {} from {} to {} (converted from {} {} to SYP)", 
                    product.getId(), product.getRefSellingPrice(), sellingPriceInSYP, requestItem.getSellingPrice(), requestCurrency);
            }
        }
        
        // Update minStockLevel if provided in the request
        if (requestItem.getMinStockLevel() != null && !requestItem.getMinStockLevel().equals(product.getMinStockLevel())) {
            product.setMinStockLevel(requestItem.getMinStockLevel());
            updated = true;
            logger.info("Updated minStockLevel for PharmacyProduct {} from {} to {}", 
                product.getId(), product.getMinStockLevel(), requestItem.getMinStockLevel());
        }
        
        // Save the product if any field was updated
        if (updated) {
            pharmacyProductRepo.save(product);
        }
    }
    
    private void updateMasterProductInformation(PurchaseInvoiceItem item, PurchaseInvoiceItemDTORequest requestItem) {
        MasterProduct product = masterProductRepo.findById(item.getProductId()).orElse(null);
        if (product == null) return;
        
        boolean updated = false;
        
        // Update minStockLevel if provided in the request
        if (requestItem.getMinStockLevel() != null && !requestItem.getMinStockLevel().equals(product.getMinStockLevel())) {
            product.setMinStockLevel(requestItem.getMinStockLevel());
            product.setUpdatedAt(LocalDateTime.now());
            updated = true;
            logger.info("Updated minStockLevel for MasterProduct {} from {} to {}", 
                product.getId(), product.getMinStockLevel(), requestItem.getMinStockLevel());
        }
        
        // Save the product if any field was updated
        if (updated) {
            masterProductRepo.save(product);
        }
    }

    // Private helper methods for product retrieval
    private List<PharmacyProduct> getPharmacyProducts(PurchaseInvoice invoice) {
        List<Long> pharmacyProductIds = invoice.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.PHARMACY)
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        return pharmacyProductRepo.findAllById(pharmacyProductIds);
    }

    private List<MasterProduct> getMasterProducts(PurchaseInvoice invoice) {
        List<Long> masterProductIds = invoice.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.MASTER)
            .map(PurchaseInvoiceItem::getProductId)
            .toList();
        return masterProductRepo.findAllById(masterProductIds);
    }

    private PurchaseInvoice getInvoiceByIdAndPharmacy(Long id, Long pharmacyId) {
        return purchaseInvoiceRepo.findByIdAndPharmacyId(id, pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase invoice not found with ID: " + id));
    }

    private List<PharmacyProduct> getAllPharmacyProducts(List<PurchaseInvoice> invoices) {
        List<Long> allPharmacyProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
        return pharmacyProductRepo.findAllById(allPharmacyProductIds);
    }

    private List<MasterProduct> getAllMasterProducts(List<PurchaseInvoice> invoices) {
        List<Long> allMasterProductIds = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseInvoiceItem::getProductId)
            .distinct()
            .toList();
        return masterProductRepo.findAllById(allMasterProductIds);
    }
    
    /**
     * Convert selling price from request currency to SYP for storage
     */
    private Double convertSellingPriceToSYP(Double sellingPrice, Currency requestCurrency) {
        if (requestCurrency == Currency.SYP) {
            return sellingPrice;
        }
        
        BigDecimal sellingPriceBigDecimal = BigDecimal.valueOf(sellingPrice);
        BigDecimal sellingPriceInSYP = exchangeRateService.convertToSYP(sellingPriceBigDecimal, requestCurrency);
        
        return sellingPriceInSYP.doubleValue();
    }
    
    /**
     * Convert any price from request currency to SYP for storage
     */
    private Double convertPriceToSYP(Double price, Currency requestCurrency) {
        if (requestCurrency == Currency.SYP) {
            return price;
        }
        
        BigDecimal priceBigDecimal = BigDecimal.valueOf(price);
        BigDecimal priceInSYP = exchangeRateService.convertToSYP(priceBigDecimal, requestCurrency);
        
        return priceInSYP.doubleValue();
    }
    
    /**
     * Convert any price from SYP to the requested currency for display
     */
    private Double convertPriceFromSYP(Double priceInSYP, Currency targetCurrency) {
        if (targetCurrency == Currency.SYP) {
            return priceInSYP;
        }
        
        BigDecimal priceBigDecimal = BigDecimal.valueOf(priceInSYP);
        BigDecimal priceInTargetCurrency = exchangeRateService.convertFromSYP(priceBigDecimal, targetCurrency);
        
        return priceInTargetCurrency.doubleValue();
    }
    
    // Helper method to get MoneyBox ID for pharmacy
    private Long getMoneyBoxIdForPharmacy(Long pharmacyId) {

      return   moneyBoxRepository.findByPharmacyId(pharmacyId).orElseThrow(
                () -> new ResourceNotFoundException("There in no moneyBox for Pharmacy with ID: " + pharmacyId)
        ).getId();
        // This would need to be implemented based on your MoneyBox service
        // For now, return pharmacyId as MoneyBox ID (assuming 1:1 relationship)
        // You'll need to implement this based on your existing MoneyBox service logic
    }
} 