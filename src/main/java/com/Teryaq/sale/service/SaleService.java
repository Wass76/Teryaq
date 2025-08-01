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
import com.Teryaq.user.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.Teryaq.sale.dto.SaleInvoiceItemDTORequest;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.user.entity.CustomerDebt;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.Teryaq.user.repository.CustomerDebtRepository;

@Service
public class SaleService {
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

    @Transactional
    public SaleInvoiceDTOResponse createSaleInvoice(SaleInvoiceDTORequest requestDTO) {
        // الحصول على العميل
        Customer customer = null;
        if (requestDTO.getCustomerId() != null) {
            customer = customerRepository.findById(requestDTO.getCustomerId()).orElse(null);
        }
        
        // التحقق من صحة الدفع
        if (!paymentValidationService.validatePayment(requestDTO.getPaymentType(), requestDTO.getPaymentMethod())) {
            throw new RuntimeException("the payment type and payment method are not compatible");
        }
        
        // التحقق من المبلغ المدفوع (سيتم التحقق من الإجمالي بعد حساب الفاتورة)
        float paidAmount = requestDTO.getPaidAmount() != null ? requestDTO.getPaidAmount() : 0;
        if (paidAmount < 0) {
            throw new RuntimeException("the paid amount cannot be negative");
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
            throw new RuntimeException("Stock items not found with IDs: " + missingIds);
        }
        
        List<SaleInvoiceItem> items = saleMapper.toEntityList(requestDTO.getItems(), stockItems);
        
        float total = 0;
        
        for (SaleInvoiceItem item : items) {
            StockItem product = item.getStockItem();
            
            if (!stockManagementService.isQuantityAvailable(product.getProductId(), item.getQuantity())) {
                String productName = stockManagementService.getProductName(product.getProductId(), product.getProductType());
                throw new RuntimeException("Insufficient stock for product: " + productName + 
                    " (ID: " + product.getProductId() + "). Available: " + 
                    stockManagementService.getTotalQuantityByProductId(product.getProductId()) + 
                    ", Requested: " + item.getQuantity());
            }
            
            // التحقق من تاريخ انتهاء الصلاحية
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                String productName = stockManagementService.getProductName(product.getProductId(), product.getProductType());
                throw new RuntimeException("Product expired: " + productName + 
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
            throw new RuntimeException("the paid amount is not valid");
        }
        
        // حساب المبلغ المتبقي
        float remainingAmount = paymentValidationService.calculateRemainingAmount(invoice.getTotalAmount(), paidAmount);
        invoice.setRemainingAmount(remainingAmount);
        
        // إذا كان الدفع نقدي ومكتمل، ضبط المبلغ المتبقي على 0
        if (requestDTO.getPaymentType() == PaymentType.CASH && 
            paymentValidationService.isPaymentComplete(invoice.getTotalAmount(), paidAmount)) {
            invoice.setRemainingAmount(0);
        }
        
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
                .amount(new BigDecimal(remainingAmount))
                .paidAmount(new BigDecimal("0.00"))
                .remainingAmount(new BigDecimal(remainingAmount))
                .dueDate(LocalDateTime.now().plusMonths(1)) // تاريخ استحقاق بعد شهر
                .notes("دين من فاتورة بيع رقم: " + invoice.getId())
                .status("ACTIVE")
                .build();
            
            customerDebtRepository.save(debt);
        } catch (Exception e) {
            // تسجيل الخطأ ولكن لا نوقف عملية البيع
            System.err.println("Error creating customer debt: " + e.getMessage());
        }
    }
} 