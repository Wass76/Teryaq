package com.Teryaq.sale.service;

import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.sale.mapper.SaleMapper;
import com.Teryaq.sale.repo.SaleInvoiceRepository;
import com.Teryaq.sale.repo.SaleInvoiceItemRepository;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.service.StockManagementService;
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
import com.Teryaq.user.repository.CustomerDebtRepository;

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
    private StockManagementService stockManagementService;
    @Autowired
    private DiscountCalculationService discountCalculationService;
    @Autowired
    private PaymentValidationService paymentValidationService;
    @Autowired
    private CustomerDebtRepository customerDebtRepository;

    @Autowired
    private SaleMapper saleMapper;

    public SaleService(SaleInvoiceRepository saleInvoiceRepository,
                      SaleInvoiceItemRepository saleInvoiceItemRepository,
                      CustomerRepo customerRepository,
                      StockItemRepo stockItemRepo,
                      StockManagementService stockManagementService,
                      DiscountCalculationService discountCalculationService,
                      PaymentValidationService paymentValidationService,
                      CustomerDebtRepository customerDebtRepository,
                      SaleMapper saleMapper,
                      com.Teryaq.user.repository.UserRepository userRepository) {
        super(userRepository);
        this.saleInvoiceRepository = saleInvoiceRepository;
        this.saleInvoiceItemRepository = saleInvoiceItemRepository;
        this.customerRepository = customerRepository;
        this.stockItemRepo = stockItemRepo;
        this.stockManagementService = stockManagementService;
        this.discountCalculationService = discountCalculationService;
        this.paymentValidationService = paymentValidationService;
        this.customerDebtRepository = customerDebtRepository;
        this.saleMapper = saleMapper;
    }

    @Transactional
    public SaleInvoiceDTOResponse createSaleInvoice(SaleInvoiceDTORequest requestDTO) {
        // Get current user's pharmacy
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        
        // الحصول على العميل
        Customer customer = null;
        if (requestDTO.getCustomerId() != null) {
            customer = customerRepository.findById(requestDTO.getCustomerId()).orElse(null);
        }
        
        // التحقق من صحة الدفع
        if (!paymentValidationService.validatePayment(requestDTO.getPaymentType(), requestDTO.getPaymentMethod())) {
            throw new ConflictException("the payment type and payment method are not compatible");
        }
        
        // التحقق من المبلغ المدفوع (سيتم التحقق من الإجمالي بعد حساب الفاتورة)
        float paidAmount = requestDTO.getPaidAmount() != null ? requestDTO.getPaidAmount() : 0;
        if (paidAmount < 0) {
            throw new ConflictException("the paid amount cannot be negative");
        }
        
        // للدفع النقدي، إذا لم يتم تحديد المبلغ المدفوع، استخدم الإجمالي
        if (requestDTO.getPaymentType() == PaymentType.CASH && paidAmount == 0) {
            // سيتم ضبط paidAmount بعد حساب الإجمالي
        }
        
        // إنشاء فاتورة البيع باستخدام Mapper
        SaleInvoice invoice = saleMapper.toEntityWithCustomerAndDate(requestDTO, customer);
        
        // الحصول على جميع StockItems المطلوبة
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
            
            if (!stockManagementService.isQuantityAvailable(product.getProductId(), item.getQuantity())) {
                String productName = stockManagementService.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Insufficient stock for product: " + productName + 
                    " (ID: " + product.getProductId() + "). Available: " + 
                    stockManagementService.getTotalQuantityByProductId(product.getProductId()) + 
                    ", Requested: " + item.getQuantity());
            }
            
            // التحقق من تاريخ انتهاء الصلاحية
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                String productName = stockManagementService.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Product expired: " + productName + 
                    " (ID: " + product.getProductId() + "). Expiry date: " + product.getExpiryDate());
            }
            
            // تحديث الكمية في المخزون
            product.setQuantity(product.getQuantity() - item.getQuantity());
            stockItemRepo.save(product);
            
            // ربط العنصر بالفاتورة
            item.setSaleInvoice(invoice);
            
            // حساب المجاميع مع الخصم الجديد
            float originalItemTotal = item.getUnitPrice() * item.getQuantity();
            float itemDiscount = discountCalculationService.calculateDiscount(
                originalItemTotal, 
                item.getDiscountType(), 
                item.getDiscount()
            );
            
            float subTotal = originalItemTotal - itemDiscount;
            item.setSubTotal(subTotal);
            total += subTotal;
        }
        
        // حساب المجاميع النهائية مع خصم الفاتورة
        float invoiceDiscount = discountCalculationService.calculateDiscount(
            total, 
            invoice.getDiscountType(), 
            invoice.getDiscount()
        );
        
        invoice.setTotalAmount(total - invoiceDiscount);
        
        // التحقق من المبلغ المدفوع بعد حساب الإجمالي
        if (!paymentValidationService.validatePaidAmount(invoice.getTotalAmount(), paidAmount, requestDTO.getPaymentType())) {
            throw new RequestNotValidException("the paid amount is not valid for payment type: " + requestDTO.getPaymentType());
        }
        
        // حساب المبلغ المتبقي
        float remainingAmount = paymentValidationService.calculateRemainingAmount(invoice.getTotalAmount(), paidAmount);
        
        // للدفع النقدي، يجب أن يكون المبلغ المتبقي 0
        if (requestDTO.getPaymentType() == PaymentType.CASH) {
            // إذا لم يتم تحديد المبلغ المدفوع أو كان 0، استخدم الإجمالي
            if (paidAmount == 0) {
                paidAmount = invoice.getTotalAmount();
                invoice.setPaidAmount(paidAmount);
            }
            
            if (remainingAmount > 0) {
                throw new RequestNotValidException("Cash payment must be complete. Remaining amount: " + remainingAmount);
            }
            remainingAmount = 0; // ضبط المبلغ المتبقي على 0 للدفع النقدي
        }
        
        invoice.setRemainingAmount(remainingAmount);
        
        invoice.setItems(items);
        
        // حفظ الفاتورة والعناصر
        SaleInvoice savedInvoice = saleInvoiceRepository.save(invoice);
        saleInvoiceItemRepository.saveAll(items);
        
        // إنشاء دين للعميل إذا كان هناك مبلغ متبقي
        if (customer != null && remainingAmount > 0) {
            createCustomerDebt(customer, remainingAmount, savedInvoice);
        }
        
        // تحويل النتيجة إلى DTO
        return saleMapper.toResponse(savedInvoice);
    }
    
    private void createCustomerDebt(Customer customer, float remainingAmount, SaleInvoice invoice) {
        try {
            CustomerDebt debt = CustomerDebt.builder()
                .customer(customer)
                .amount(remainingAmount)
                .paidAmount(0f)
                .remainingAmount(remainingAmount)
                .dueDate(LocalDate.now().plusMonths(1)) // تاريخ استحقاق بعد شهر
                .notes("دين من فاتورة بيع رقم: " + invoice.getId())
                .status("ACTIVE")
                .build();
            
            customerDebtRepository.save(debt);
        } catch (Exception e) {
            // تسجيل الخطأ ولكن لا نوقف عملية البيع
            logger.error("Error creating customer debt for invoice {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }

    /**
     * الحصول على فاتورة بيع بواسطة المعرف
     */
    public SaleInvoiceDTOResponse getSaleById(Long saleId) {
        // Get current user's pharmacy ID
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // Find sale invoice by ID and pharmacy ID to ensure pharmacy isolation
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));
        return saleMapper.toResponse(saleInvoice);
    }

    /**
     * إلغاء عملية البيع واستعادة الكميات في المخزون
     */
    @Transactional
    public void cancelSale(Long saleId) {
        // Get current user's pharmacy ID for security
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // البحث عن فاتورة البيع with pharmacy filtering
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));

        // التحقق من أن الفاتورة لم يتم دفعها بالكامل (لا يمكن إلغاء فواتير مدفوعة بالكامل)
        if (saleInvoice.getRemainingAmount() <= 0) {
            throw new RequestNotValidException("Cannot cancel a fully paid sale invoice");
        }

        // استعادة الكميات في المخزون
        for (SaleInvoiceItem item : saleInvoice.getItems()) {
            StockItem stockItem = item.getStockItem();
            if (stockItem != null) {
                // إضافة الكمية المباعة مرة أخرى إلى المخزون
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                stockItemRepo.save(stockItem);
            }
        }

        // حذف ديون العميل المرتبطة بهذه الفاتورة
        if (saleInvoice.getCustomer() != null) {
            List<CustomerDebt> relatedDebts = customerDebtRepository.findByCustomerId(saleInvoice.getCustomer().getId());
            relatedDebts.stream()
                    .filter(debt -> debt.getNotes() != null && debt.getNotes().contains("دين من فاتورة بيع رقم: " + saleId))
                    .forEach(debt -> customerDebtRepository.delete(debt));
        }

        // حذف عناصر الفاتورة
        saleInvoiceItemRepository.deleteBySaleInvoiceId(saleId);

        // حذف الفاتورة
        saleInvoiceRepository.delete(saleInvoice);
    }
} 