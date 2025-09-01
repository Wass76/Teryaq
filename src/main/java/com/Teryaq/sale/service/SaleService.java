package com.Teryaq.sale.service;

import com.Teryaq.moneybox.service.SalesIntegrationService;
import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.sale.enums.InvoiceStatus;
import com.Teryaq.sale.enums.PaymentStatus;
import com.Teryaq.sale.enums.RefundStatus;
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
import com.Teryaq.utils.exception.UnAuthorizedException;

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
import com.Teryaq.user.mapper.CustomerDebtMapper;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.sale.dto.SaleRefundDTORequest;
import com.Teryaq.sale.dto.SaleRefundDTOResponse;
import com.Teryaq.sale.dto.SaleRefundItemDTORequest;
import com.Teryaq.sale.entity.SaleRefund;
import com.Teryaq.sale.entity.SaleRefundItem;
import com.Teryaq.sale.mapper.SaleRefundMapper;
import com.Teryaq.sale.repo.SaleRefundRepo;
import com.Teryaq.sale.repo.SaleRefundItemRepo;
import com.Teryaq.product.Enum.PaymentMethod;
import java.util.ArrayList;

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
    private SalesIntegrationService salesIntegrationService;

    @Autowired
    private SaleMapper saleMapper;
    
    @Autowired
    private CustomerDebtMapper customerDebtMapper;

    @Autowired
    private SaleRefundRepo saleRefundRepo;
    @Autowired
    private SaleRefundItemRepo saleRefundItemRepo;
    @Autowired
    private SaleRefundMapper saleRefundMapper;

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
                       SalesIntegrationService salesIntegrationService,
                       CustomerDebtMapper customerDebtMapper,
                       UserRepository userRepository) {
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
        this.salesIntegrationService = salesIntegrationService;
        this.customerDebtMapper = customerDebtMapper;
    }

    @Transactional
    public SaleInvoiceDTOResponse createSaleInvoice(SaleInvoiceDTORequest requestDTO) {
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        if (currentPharmacy == null) {
            throw new UnAuthorizedException("You are not authorized to create a sale invoice");
        }
        
        Customer customer = null;
        if (requestDTO.getCustomerId() != null) {
            customer = customerRepository.findById(requestDTO.getCustomerId()).orElse(null);
        } else {
            customer = getOrCreateCashCustomer(currentPharmacy);
        }
        
        if (!paymentValidationService.validatePayment(requestDTO.getPaymentType(), requestDTO.getPaymentMethod())) {
            throw new ConflictException("the payment type and payment method are not compatible");
        }
        
        if (customer == null) {
            throw new ConflictException("Cannot create sale invoice without a customer");
        }
        
        // التحقق من أن العميل ينتمي للصيدلية الحالية
        if (!customer.getPharmacy().getId().equals(currentPharmacy.getId())) {
            throw new ConflictException("Customer with ID " + customer.getId() + 
                " does not belong to the current pharmacy. Customer belongs to pharmacy: " + 
                customer.getPharmacy().getName());
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
        
        // حساب الحالات الجديدة
        calculateInvoiceStatuses(invoice);
        
        invoice.setItems(items);
        
        SaleInvoice savedInvoice = saleInvoiceRepository.save(invoice);
        saleInvoiceItemRepository.saveAll(items);
        
        // Integrate with Money Box for cash payments
        if (requestDTO.getPaymentMethod() == com.Teryaq.product.Enum.PaymentMethod.CASH) {
            try {
                // Get current pharmacy ID for MoneyBox integration
                Long currentPharmacyId = getCurrentUserPharmacyId();
                salesIntegrationService.recordSalePayment(
                    currentPharmacyId,
                    savedInvoice.getId(),
                    java.math.BigDecimal.valueOf(savedInvoice.getTotalAmount()),
                    requestDTO.getCurrency()
                );
                logger.info("Cash sale recorded in Money Box for invoice: {}", savedInvoice.getId());
            } catch (Exception e) {
                logger.warn("Failed to record cash sale in Money Box for invoice {}: {}", 
                           savedInvoice.getId(), e.getMessage());
                // Don't fail the sale if Money Box integration fails
            }
        }
        
        if (customer != null && remainingAmount > 0) {
            createCustomerDebt(customer, remainingAmount, savedInvoice, requestDTO);
        }
        
        return saleMapper.toResponse(savedInvoice);
    }
    
    private void createCustomerDebt(Customer customer, float remainingAmount, SaleInvoice invoice, SaleInvoiceDTORequest request) {
        try {
            // التحقق من أن الفاتورة في حالة SOLD قبل إنشاء الدين
            if (invoice.getStatus() != InvoiceStatus.SOLD) {
                logger.warn("Cannot create debt for invoice {} with status: {}", invoice.getId(), invoice.getStatus());
                return;
            }
            
            LocalDate dueDate = request.getDebtDueDate() != null ? request.getDebtDueDate() : LocalDate.now().plusMonths(1);
            
            CustomerDebt debt = customerDebtMapper.toEntityFromSaleInvoice(request, remainingAmount, dueDate);
            debt.setCustomer(customer);
            debt.setNotes("Debt from sale invoice: " + invoice.getId());
            
            customerDebtRepository.save(debt);
            logger.info("Created customer debt: {} for invoice: {} using mapper", remainingAmount, invoice.getId());
        } catch (Exception e) {
            logger.error("Error creating customer debt for invoice {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }

  
    private Customer getOrCreateCashCustomer(Pharmacy pharmacy) {
        try {
            Customer cashCustomer = customerRepository.findByNameAndPharmacyId("cash customer", pharmacy.getId())
                .orElse(null);
            
            if (cashCustomer == null) {
                cashCustomer = new Customer();
                cashCustomer.setName("cash customer");
                cashCustomer.setPhoneNumber("0000000000");
                cashCustomer.setAddress("pharmacy " + pharmacy.getName());
                cashCustomer.setPharmacy(pharmacy);
                cashCustomer.setNotes("cash customer");
                
                cashCustomer = customerRepository.save(cashCustomer);
                logger.info("Created cash customer for pharmacy: {}", pharmacy.getId());
            }
            
            return cashCustomer;
        } catch (Exception e) {
            logger.error("Error creating cash customer for pharmacy {}: {}", pharmacy.getId(), e.getMessage());
            return null;
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

        // التحقق من أن الفاتورة في حالة SOLD قبل الإلغاء
        if (saleInvoice.getStatus() != InvoiceStatus.SOLD) {
            throw new RequestNotValidException("Cannot cancel invoice with status: " + saleInvoice.getStatus() + 
                ". Only SOLD invoices can be cancelled.");
        }

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

    @Transactional
    public SaleRefundDTOResponse processRefund(Long saleId, SaleRefundDTORequest request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));

        // التحقق من أن الفاتورة لم يتم إرجاعها كلياً
        if (saleInvoice.getRefundStatus() == RefundStatus.FULLY_REFUNDED) {
            throw new RequestNotValidException("Sale invoice has already been fully refunded");
        }

        // التحقق من أن الفاتورة مدفوعة بالكامل
        if (saleInvoice.getRemainingAmount() > 0) {
            throw new RequestNotValidException("Cannot refund a partially paid sale invoice. Remaining amount: " + saleInvoice.getRemainingAmount());
        }

        SaleRefund refund = saleRefundMapper.toEntity(request, saleInvoice, getCurrentUserPharmacy());
        List<SaleRefundItem> refundItems = new ArrayList<>();
        float totalRefundAmount = 0.0f;

        // معالجة المنتجات المرتجعة
        totalRefundAmount = processRefundItems(saleInvoice, request, refund, refundItems);

        refund.setTotalRefundAmount(totalRefundAmount);
        refund.setRefundItems(refundItems);
        
        // حفظ المرتجعات
        SaleRefund savedRefund = saleRefundRepo.save(refund);
        saleRefundItemRepo.saveAll(refundItems);
        
        // تحديث المخزون
        restoreStock(refundItems);
        refund.setStockRestored(true);
        saleRefundRepo.save(refund);

        // تسجيل المرتجعات في MoneyBox
        if (saleInvoice.getPaymentMethod() == PaymentMethod.CASH) {
            try {
                salesIntegrationService.recordSaleRefund(
                    currentPharmacyId,
                    saleId,
                    java.math.BigDecimal.valueOf(totalRefundAmount),
                    saleInvoice.getCurrency()
                );
                logger.info("Sale refund recorded in Money Box for invoice: {}", saleId);
            } catch (Exception e) {
                logger.warn("Failed to record sale refund in Money Box for invoice {}: {}", 
                           saleId, e.getMessage());
            }
        }

        // تحديث حالة الفاتورة
        updateInvoiceStatus(saleInvoice);

        return saleRefundMapper.toResponse(savedRefund);
    }

    private float processRefundItems(SaleInvoice saleInvoice, SaleRefundDTORequest request, 
                                   SaleRefund refund, List<SaleRefundItem> refundItems) {
        float totalRefundAmount = 0.0f;
        
        if (request.getRefundItems() == null || request.getRefundItems().isEmpty()) {
            throw new RequestNotValidException("Refund items list is required");
        }

        for (SaleRefundItemDTORequest refundRequest : request.getRefundItems()) {
            SaleInvoiceItem originalItem = saleInvoice.getItems().stream()
                    .filter(item -> item.getId().equals(refundRequest.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RequestNotValidException("Item not found with ID: " + refundRequest.getItemId()));

            // حساب الكمية المتاحة للإرجاع (الكمية المباعة - الكمية المرتجعة مسبقاً)
            int availableForRefund = originalItem.getQuantity() - originalItem.getRefundedQuantity();
            
            if (refundRequest.getQuantity() > availableForRefund) {
                throw new RequestNotValidException("Refund quantity cannot exceed available quantity for item ID: " + 
                    refundRequest.getItemId() + ". Available: " + availableForRefund + ", Requested: " + refundRequest.getQuantity());
            }

            SaleRefundItem refundItem = new SaleRefundItem();
            refundItem.setSaleRefund(refund);
            refundItem.setSaleInvoiceItem(originalItem);
            refundItem.setRefundQuantity(refundRequest.getQuantity());
            refundItem.setUnitPrice(originalItem.getUnitPrice());
            refundItem.setSubtotal(originalItem.getUnitPrice() * refundRequest.getQuantity());
            refundItem.setItemRefundReason(refundRequest.getItemRefundReason());
            refundItem.setStockRestored(false);
            
            refundItems.add(refundItem);
            totalRefundAmount += refundItem.getSubtotal();
            
            // تحديث الكمية المرتجعة في العنصر الأصلي
            originalItem.setRefundedQuantity(originalItem.getRefundedQuantity() + refundRequest.getQuantity());
        }
        
        return totalRefundAmount;
    }

    private void restoreStock(List<SaleRefundItem> refundItems) {
        for (SaleRefundItem refundItem : refundItems) {
            SaleInvoiceItem originalItem = refundItem.getSaleInvoiceItem();
            StockItem stockItem = originalItem.getStockItem();
            
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + refundItem.getRefundQuantity());
                stockItemRepo.save(stockItem);
                refundItem.setStockRestored(true);
            }
        }
    }

    /**
     * تحديث حالة الفاتورة بناءً على الكميات المرتجعة
     */
    private void updateInvoiceStatus(SaleInvoice saleInvoice) {
        boolean allItemsFullyRefunded = true;
        boolean hasAnyRefund = false;
        
        for (SaleInvoiceItem item : saleInvoice.getItems()) {
            if (item.getRefundedQuantity() > 0) {
                hasAnyRefund = true;
            }
            if (item.getRefundedQuantity() < item.getQuantity()) {
                allItemsFullyRefunded = false;
            }
        }
        
        // تحديث حالة المرتجعات
        if (allItemsFullyRefunded && hasAnyRefund) {
            saleInvoice.setRefundStatus(RefundStatus.FULLY_REFUNDED);
        } else if (hasAnyRefund) {
            saleInvoice.setRefundStatus(RefundStatus.PARTIALLY_REFUNDED);
        } else {
            saleInvoice.setRefundStatus(RefundStatus.NO_REFUND);
        }
        
        // تحديث حالة الدفع (إذا كان هناك مرتجعات، قد تحتاج لتحديث المبالغ)
        if (saleInvoice.getRemainingAmount() == 0) {
            saleInvoice.setPaymentStatus(PaymentStatus.FULLY_PAID);
        } else if (saleInvoice.getPaidAmount() > 0) {
            saleInvoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            saleInvoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        
        // حالة الفاتورة الأساسية تبقى SOLD
        saleInvoice.setStatus(InvoiceStatus.SOLD);
        
        saleInvoiceRepository.save(saleInvoice);
    }

    /**
     * حساب الحالات الجديدة للفاتورة
     */
    private void calculateInvoiceStatuses(SaleInvoice invoice) {
        // حساب حالة الدفع
        if (invoice.getRemainingAmount() == 0) {
            invoice.setPaymentStatus(PaymentStatus.FULLY_PAID);
        } else if (invoice.getPaidAmount() > 0) {
            invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        
        // حساب حالة المرتجعات (افتراضياً لا توجد مرتجعات عند الإنشاء)
        invoice.setRefundStatus(RefundStatus.NO_REFUND);
        
        // حالة الفاتورة الأساسية
        invoice.setStatus(InvoiceStatus.SOLD);
    }

    public List<SaleRefundDTOResponse> getRefundsBySaleId(Long saleId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findBySaleInvoiceIdAndPharmacyId(saleId, currentPharmacyId);
        
        return refunds.stream()
                .map(saleRefundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleRefundDTOResponse> getAllRefunds() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findByPharmacyIdOrderByRefundDateDesc(currentPharmacyId);
        
        return refunds.stream()
                .map(saleRefundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleRefundDTOResponse> getRefundsByDateRange(LocalDate startDate, LocalDate endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findByPharmacyIdAndRefundDateBetween(
            currentPharmacyId, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
        
        return refunds.stream()
                .map(saleRefundMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * جلب الفواتير المدفوعة بالكامل
     */
    public List<SaleInvoiceDTOResponse> getFullyPaidSales() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == PaymentStatus.FULLY_PAID)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * جلب الفواتير التي فيها دين
     */
    public List<SaleInvoiceDTOResponse> getInvoicesWithDebt() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID || 
                                 invoice.getPaymentStatus() == PaymentStatus.UNPAID)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * جلب الفواتير حسب حالة الدفع
     */
    public List<SaleInvoiceDTOResponse> getSalesByPaymentStatus(PaymentStatus paymentStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == paymentStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * جلب الفواتير حسب حالة المرتجعات
     */
    public List<SaleInvoiceDTOResponse> getSalesByRefundStatus(RefundStatus refundStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getRefundStatus() == refundStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * جلب الفواتير حسب الحالة الأساسية
     */
    public List<SaleInvoiceDTOResponse> getSalesByInvoiceStatus(InvoiceStatus invoiceStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getStatus() == invoiceStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
} 