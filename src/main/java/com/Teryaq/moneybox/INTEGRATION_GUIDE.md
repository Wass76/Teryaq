# MoneyBox Integration Guide

## Overview
This document explains how to properly integrate the MoneyBox feature with sales and purchase operations in the Teryaq Pharmacy Management System.

## Integration Approach

### ✅ **Recommended Approach: Direct Service Integration**

The **correct and logical approach** is to integrate MoneyBox transactions directly into the sales and purchase services, ensuring:

1. **Atomic Operations**: Either both the sale/purchase AND the money box transaction succeed, or they both fail
2. **Data Consistency**: No orphaned transactions or operations
3. **Better User Experience**: Single operation instead of multiple API calls
4. **Proper Error Handling**: If money box update fails, the entire operation is rolled back

### ❌ **Not Recommended: Separate Integration Endpoints**

Creating separate endpoints for integration is **not recommended** because:
- Requires multiple API calls
- Risk of partial failures
- More complex error handling
- Poorer user experience
- Potential data inconsistency

## Implementation Details

### Sales Service Integration

```java
@Service
public class SaleService {
    
    private final SalesIntegrationService salesIntegrationService;
    
    @Transactional
    public SaleInvoiceDTOResponse createSale(SaleInvoiceDTORequest request) {
        // ... existing sale creation logic ...
        
        SaleInvoice savedInvoice = saleInvoiceRepository.save(invoice);
        
        // ✅ INTEGRATE MONEYBOX HERE - Within the same transaction
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            Long currentPharmacyId = getCurrentUserPharmacyId();
            salesIntegrationService.recordSalePayment(
                currentPharmacyId,
                savedInvoice.getId(),
                BigDecimal.valueOf(savedInvoice.getTotalAmount()),
                request.getCurrency().toString()
            );
            // If MoneyBox update fails, the entire sale operation is rolled back
        }
        
        return saleMapper.toResponse(savedInvoice);
    }
}
```

### Purchase Service Integration

```java
@Service
public class PurchaseInvoiceService {
    
    private final PurchaseIntegrationService purchaseIntegrationService;
    
    @Transactional
    public PurchaseInvoiceDTOResponse createPurchase(PurchaseInvoiceDTORequest request) {
        // ... existing purchase creation logic ...
        
        PurchaseInvoice saved = purchaseInvoiceRepo.save(invoice);
        
        // ✅ INTEGRATE MONEYBOX HERE - Within the same transaction
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            Long currentPharmacyId = getCurrentUserPharmacyId();
            purchaseIntegrationService.recordPurchasePayment(
                currentPharmacyId,
                saved.getId(),
                BigDecimal.valueOf(saved.getTotal()),
                request.getCurrency().toString()
            );
            // If MoneyBox update fails, the entire purchase operation is rolled back
        }
        
        return purchaseInvoiceMapper.toResponse(saved);
    }
}
```

## Key Integration Points

### 1. **Sales Operations**
- **Sale Creation**: `SalesIntegrationService.recordSalePayment()`
- **Sale Refund**: `SalesIntegrationService.recordSaleRefund()`

### 2. **Purchase Operations**
- **Purchase Creation**: `PurchaseIntegrationService.recordPurchasePayment()`
- **Purchase Refund**: `PurchaseIntegrationService.recordPurchaseRefund()`

### 3. **Other Financial Operations**
- **Expense Recording**: `PurchaseIntegrationService.recordExpense()`
- **Income Recording**: `PurchaseIntegrationService.recordIncome()`

## Transactional Behavior

### Transaction Propagation
All MoneyBox integration methods use `@Transactional(propagation = Propagation.REQUIRED)`, which means:

- If called within an existing transaction, they participate in that transaction
- If no transaction exists, they create a new one
- If the MoneyBox operation fails, the entire operation is rolled back

### Error Handling
```java
try {
    salesIntegrationService.recordSalePayment(pharmacyId, saleId, amount, currency);
    // Success - both sale and MoneyBox transaction are committed
} catch (Exception e) {
    // Failure - both sale and MoneyBox transaction are rolled back
    // The exception is propagated to the caller
}
```

## Benefits of This Approach

### 1. **Data Integrity**
- ✅ Ensures consistency between sales/purchases and MoneyBox
- ✅ No orphaned records or inconsistent states
- ✅ Atomic operations guarantee all-or-nothing behavior

### 2. **Performance**
- ✅ Single database transaction
- ✅ No additional API calls
- ✅ Faster operation completion

### 3. **User Experience**
- ✅ Single operation from user perspective
- ✅ Clear success/failure feedback
- ✅ No partial states to handle

### 4. **Maintenance**
- ✅ Simpler code to maintain
- ✅ Easier to debug and test
- ✅ Fewer moving parts

## Implementation Status

### ✅ **Completed**
- `SalesIntegrationService` with proper transactional behavior
- `PurchaseIntegrationService` with proper transactional behavior
- Integration methods updated in `SaleService`
- Integration methods updated in `PurchaseInvoiceService`

### 🔄 **In Progress**
- Error handling improvements
- Comprehensive testing
- Performance optimization

### 📋 **Future Enhancements**
- Refund processing integration
- Bulk transaction support
- Advanced reporting integration

## Best Practices

### 1. **Always Use Within Transactions**
```java
@Transactional
public void someBusinessOperation() {
    // ... business logic ...
    
    // MoneyBox integration within the same transaction
    salesIntegrationService.recordSalePayment(pharmacyId, saleId, amount, currency);
    
    // If MoneyBox fails, everything is rolled back
}
```

### 2. **Proper Error Handling**
```java
// Let exceptions propagate naturally
// The transaction will handle rollback automatically
salesIntegrationService.recordSalePayment(pharmacyId, saleId, amount, currency);
```

### 3. **Currency Consistency**
```java
// Ensure currency matches between sale and MoneyBox
String currency = request.getCurrency().toString();
salesIntegrationService.recordSalePayment(pharmacyId, saleId, amount, currency);
```

### 4. **Pharmacy ID Security**
```java
// Always use the current user's pharmacy ID for security
Long currentPharmacyId = getCurrentUserPharmacyId();
salesIntegrationService.recordSalePayment(currentPharmacyId, saleId, amount, currency);
```

## Conclusion

This integration approach ensures that MoneyBox transactions are always consistent with sales and purchase operations, providing a robust and reliable financial management system for the pharmacy.

The key is to treat MoneyBox integration as an **integral part** of the business operations rather than a separate concern, ensuring data consistency and proper transactional behavior.
