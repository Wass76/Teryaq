package com.Teryaq.sale.service;

import com.Teryaq.moneybox.service.MoneyBoxIntegrationService;
import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.sale.mapper.SaleMapper;
import com.Teryaq.sale.repo.SaleInvoiceRepository;
import com.Teryaq.sale.repo.SaleInvoiceItemRepository;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.service.StockService;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.repository.CustomerRepo;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.RequestNotValidException;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.Teryaq.sale.dto.SaleInvoiceItemDTORequest;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.user.entity.CustomerDebt;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.Teryaq.user.repository.CustomerDebtRepository;
import com.Teryaq.product.mapper.StockItemMapper;

@Service
public class SaleService extends BaseSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    @Autowired
    private SaleInvoiceRepository saleInvoiceRepository;
    @Autowired
    private SaleInvoiceItemRepository saleInvoiceItemRepository;
    @Autowired
    private CustomerRepo customerRepository;
    @Autowired
    private StockItemRepo stockItemRepo;
    @Autowired
    private StockService stockService;
    @Autowired
    private DiscountCalculationService discountCalculationService;
    @Autowired
    private PaymentValidationService paymentValidationService;
    @Autowired
    private CustomerDebtRepository customerDebtRepository;
    @Autowired
    private StockItemMapper stockItemMapper;
    @Autowired
    private MoneyBoxIntegrationService moneyBoxIntegrationService;

    @Autowired
    private SaleMapper saleMapper;

    public SaleService(SaleInvoiceRepository saleInvoiceRepository,
                      SaleInvoiceItemRepository saleInvoiceItemRepository,
                      CustomerRepo customerRepository,
                      StockItemRepo stockItemRepo,
                      StockService stockService,
                      DiscountCalculationService discountCalculationService,
                      PaymentValidationService paymentValidationService,
                      CustomerDebtRepository customerDebtRepository,
                      SaleMapper saleMapper,
                      StockItemMapper stockItemMapper,
                      MoneyBoxIntegrationService moneyBoxIntegrationService,
                      com.Teryaq.user.repository.UserRepository userRepository) {
        super(userRepository);
        this.saleInvoiceRepository = saleInvoiceRepository;
        this.saleInvoiceItemRepository = saleInvoiceItemRepository;
        this.customerRepository = customerRepository;
        this.stockItemRepo = stockItemRepo;
        this.stockService = stockService;
        this.discountCalculationService = discountCalculationService;
        this.paymentValidationService = paymentValidationService;
        this.customerDebtRepository = customerDebtRepository;
        this.saleMapper = saleMapper;
        this.stockItemMapper = stockItemMapper;
        this.moneyBoxIntegrationService = moneyBoxIntegrationService;
    }

    @Transactional
    public SaleInvoiceDTOResponse createSaleInvoice(SaleInvoiceDTORequest requestDTO) {
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        
        Customer customer = null;
        if (requestDTO.getCustomerId() != null) {
            customer = customerRepository.findById(requestDTO.getCustomerId()).orElse(null);
        }
        
        if (!paymentValidationService.validatePayment(requestDTO.getPaymentType(), requestDTO.getPaymentMethod())) {
            throw new ConflictException("the payment type and payment method are not compatible");
        }
        
        float paidAmount = requestDTO.getPaidAmount() != null ? requestDTO.getPaidAmount() : 0;
        if (paidAmount < 0) {
            throw new ConflictException("the paid amount cannot be negative");
        }
        
        SaleInvoice invoice = saleMapper.toEntityWithCustomerAndDate(requestDTO, customer, currentPharmacy);
        
        // Generate invoice number
        String invoiceNumber = "INV-" + System.currentTimeMillis() + "-" + currentPharmacy.getId();
        invoice.setInvoiceNumber(invoiceNumber);
        
        List<Long> stockItemIds = requestDTO.getItems().stream()
            .map(SaleInvoiceItemDTORequest::getStockItemId)
            .collect(Collectors.toList());
        
        List<StockItem> stockItems = stockItemRepo.findAllById(stockItemIds);
        
        if (stockItems.size() != stockItemIds.size()) {
            List<Long> foundIds = stockItems.stream().map(StockItem::getId).collect(Collectors.toList());
            List<Long> missingIds = stockItemIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
            throw new EntityNotFoundException("Stock items not found with IDs: " + missingIds);
        }
        
        List<SaleInvoiceItem> items = saleMapper.toEntityList(requestDTO.getItems(), stockItems);
        
        float total = 0;
        
        for (SaleInvoiceItem item : items) {
            StockItem product = item.getStockItem();
            
            if (!stockService.isQuantityAvailable(product.getProductId(), item.getQuantity(), product.getProductType())) {
                String productName = stockItemMapper.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Insufficient stock for product: " + productName + 
                    " (ID: " + product.getProductId() + "). Available: " + 
                    stockItemRepo.getTotalQuantity(product.getProductId(), getCurrentUserPharmacyId(), product.getProductType()) + 
                    ", Requested: " + item.getQuantity());
            }
            
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                String productName = stockItemMapper.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Product expired: " + productName + 
                    " (ID: " + product.getProductId() + "). Expiry date: " + product.getExpiryDate());
            }
            
            product.setQuantity(product.getQuantity() - item.getQuantity());
            stockItemRepo.save(product);
            
            item.setSaleInvoice(invoice);
            
            float subTotal = item.getUnitPrice() * item.getQuantity();
            item.setSubTotal(subTotal);
            total += subTotal;
        }
        
        float invoiceDiscount = discountCalculationService.calculateDiscount(
            total, 
            invoice.getDiscountType(), 
            invoice.getDiscount()
        );
        
        invoice.setTotalAmount(total - invoiceDiscount);
        
        if (requestDTO.getPaymentType() == PaymentType.CASH && paidAmount == 0) {
            paidAmount = invoice.getTotalAmount();
        }
        
        if (!paymentValidationService.validatePaidAmount(invoice.getTotalAmount(), paidAmount, requestDTO.getPaymentType())) {
            throw new RequestNotValidException("the paid amount is not valid for payment type: " + requestDTO.getPaymentType());
        }
        
        float remainingAmount = paymentValidationService.calculateRemainingAmount(invoice.getTotalAmount(), paidAmount);
        
        if (requestDTO.getPaymentType() == PaymentType.CASH) {
            if (remainingAmount > 0) {
                throw new RequestNotValidException("Cash payment must be complete. Remaining amount: " + remainingAmount);
            }
            remainingAmount = 0; 
        }
        
        invoice.setPaidAmount(paidAmount);
        invoice.setRemainingAmount(remainingAmount);
        
        invoice.setItems(items);
        
        SaleInvoice savedInvoice = saleInvoiceRepository.save(invoice);
        saleInvoiceItemRepository.saveAll(items);
        
        // Integrate with Money Box for cash payments
        if (requestDTO.getPaymentMethod() == com.Teryaq.product.Enum.PaymentMethod.CASH) {
            try {
                moneyBoxIntegrationService.recordCashSale(
                    java.math.BigDecimal.valueOf(savedInvoice.getTotalAmount()),
                    requestDTO.getCurrency().toString(),
                    savedInvoice.getId(),
                    "INV-" + savedInvoice.getId() // Use ID instead of invoice number
                );
                logger.info("Cash sale recorded in Money Box for invoice: {}", savedInvoice.getId());
            } catch (Exception e) {
                logger.warn("Failed to record cash sale in Money Box for invoice {}: {}", 
                           savedInvoice.getId(), e.getMessage());
                // Don't fail the sale if Money Box integration fails
            }
        }
        
        if (customer != null && remainingAmount > 0) {
            createCustomerDebt(customer, remainingAmount, savedInvoice);
        }
        
        return saleMapper.toResponse(savedInvoice);
    }
    
    private void createCustomerDebt(Customer customer, float remainingAmount, SaleInvoice invoice) {
        try {
            CustomerDebt debt = CustomerDebt.builder()
                .customer(customer)
                .amount(remainingAmount)
                .paidAmount(0f)
                .remainingAmount(remainingAmount)
                .dueDate(LocalDate.now().plusMonths(1)) 
                .notes("دين من فاتورة بيع رقم: " + invoice.getId())
                .status("ACTIVE")
                .build();
            
            customerDebtRepository.save(debt);
        } catch (Exception e) {
            logger.error("Error creating customer debt for invoice {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }

   
    public SaleInvoiceDTOResponse getSaleById(Long saleId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));
        return saleMapper.toResponse(saleInvoice);
    }

    
    @Transactional
    public void cancelSale(Long saleId) {
        // Get current user's pharmacy ID for security
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));

        if (saleInvoice.getRemainingAmount() <= 0) {
            throw new RequestNotValidException("Cannot cancel a fully paid sale invoice");
        }

        for (SaleInvoiceItem item : saleInvoice.getItems()) {
            StockItem stockItem = item.getStockItem();
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                stockItemRepo.save(stockItem);
            }
        }

        if (saleInvoice.getCustomer() != null) {
            List<CustomerDebt> relatedDebts = customerDebtRepository.findByCustomerId(saleInvoice.getCustomer().getId());
            relatedDebts.stream()
                    .filter(debt -> debt.getNotes() != null && debt.getNotes().contains("دين من فاتورة بيع رقم: " + saleId))
                    .forEach(debt -> customerDebtRepository.delete(debt));
        }

        saleInvoiceItemRepository.deleteBySaleInvoiceId(saleId);

        saleInvoiceRepository.delete(saleInvoice);
    }


    public List<SaleInvoiceDTOResponse> getAllSales() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleInvoiceDTOResponse> searchSaleInvoiceByDate(LocalDate createdDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDateTime startOfDay = createdDate.atStartOfDay();
        LocalDateTime endOfDay = createdDate.atTime(23, 59, 59);
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdAndInvoiceDateBetween(currentPharmacyId, startOfDay, endOfDay);
        if (saleInvoices.isEmpty()) {
            throw new EntityNotFoundException("No sale invoices found for date: " + createdDate);
        }
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SaleInvoiceDTOResponse> searchSaleInvoiceByDateRange(LocalDate startDate, LocalDate endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdAndInvoiceDateBetween(
            currentPharmacyId, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
        
        if (saleInvoices.isEmpty()) {
            throw new EntityNotFoundException("No sale invoices found between " + startDate + " and " + endDate);
        }
        
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
} 