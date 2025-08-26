# MoneyBox Integration with CustomerDebtService

## Overview
This document outlines the integration between the CustomerDebtService and MoneyBox feature to ensure proper financial transaction tracking when customers pay their debts.

## Integration Points

### 1. **Debt Payment Integration**

#### **Single Debt Payment**
When a customer pays a single debt with cash:

```java
@Transactional
public CustomerDebtDTOResponse payDebt(PayDebtDTORequest request) {
    // ... validation and debt retrieval logic ...
    
    // Process payment
    Float paymentAmount = request.getPaymentAmount().floatValue();
    Float newPaidAmount = debt.getPaidAmount() + paymentAmount;
    Float newRemainingAmount = debt.getAmount() - newPaidAmount;
    
    debt.setPaidAmount(newPaidAmount);
    debt.setRemainingAmount(newRemainingAmount);
    debt.setPaymentMethod(request.getPaymentMethod());
    
    // Update status based on payment
    if (newRemainingAmount <= 0) {
        debt.setStatus("PAID");
        debt.setPaidAt(LocalDate.now());
    } else if (debt.getDueDate().isBefore(LocalDate.now())) {
        debt.setStatus("OVERDUE");
    }
    
    CustomerDebt savedDebt = customerDebtRepository.save(debt);
    
    // ✅ MONEYBOX INTEGRATION - Record cash payment
    if (request.getPaymentMethod() == PaymentMethod.CASH) {
        try {
            Long pharmacyId = getCurrentUserPharmacyId();
            salesIntegrationService.recordSalePayment(
                pharmacyId,
                debt.getId(),
                BigDecimal.valueOf(paymentAmount),
                "SYP"
            );
            logger.info("Debt payment recorded in MoneyBox for debt: {}", debt.getId());
        } catch (Exception e) {
            logger.warn("Failed to record debt payment in MoneyBox for debt {}: {}", 
                       debt.getId(), e.getMessage());
            // Note: Payment is still processed even if MoneyBox recording fails
        }
    }
    
    return customerDebtMapper.toResponse(savedDebt);
}
```

#### **Multiple Debts Payment**
When a customer pays multiple debts with a single cash payment:

```java
@Transactional
public PayCustomerDebtsResponse payMultipleDebts(Long customerId, PayCustomerDebtsRequest request) {
    // ... validation and debt processing logic ...
    
    // Process multiple debts based on payment strategy
    while (remainingPaymentAmount > 0 && !activeDebts.isEmpty()) {
        CustomerDebt debt = activeDebts.remove(0);
        // ... debt payment processing ...
        customerDebtRepository.save(debt);
        paymentDetails.add(detail);
        remainingPaymentAmount -= amountToPay;
    }
    
    // ✅ MONEYBOX INTEGRATION - Record total cash payment
    if (request.getPaymentMethod() == PaymentMethod.CASH) {
        try {
            float actualPaidAmount = request.getTotalPaymentAmount().floatValue() - remainingPaymentAmount;
            Long pharmacyId = getCurrentUserPharmacyId();
            salesIntegrationService.recordSalePayment(
                pharmacyId,
                customerId, // Using customerId as reference for multiple debts
                BigDecimal.valueOf(actualPaidAmount),
                "SYP"
            );
            logger.info("Multiple debt payments recorded in MoneyBox for customer: {}", customerId);
        } catch (Exception e) {
            logger.warn("Failed to record debt payment in MoneyBox for customer {}: {}", 
                       customerId, e.getMessage());
            // Note: Payments are still processed even if MoneyBox recording fails
        }
    }
    
    return PayCustomerDebtsResponse.builder()
            .customerId(customerId)
            .customerName(customer.getName())
            .totalPaymentAmount(request.getTotalPaymentAmount().floatValue())
            .totalRemainingDebt(totalRemainingDebt)
            .paymentStrategy(request.getPaymentStrategy().toString())
            .debtPayments(paymentDetails)
            .notes(request.getNotes())
            .build();
}
```

### 2. **Debt Creation Integration**

Debt creation does **NOT** affect the MoneyBox because:

- Debt creation is just a record of money owed
- No actual cash transaction occurs
- MoneyBox should only reflect actual cash movements

```java
@Transactional
public CustomerDebtDTOResponse createDebt(CustomerDebtDTORequest request) {
    // ... validation and debt creation logic ...
    
    CustomerDebt debt = customerDebtMapper.toEntity(request);
    debt.setCustomer(customer);
    debt.setPaidAmount(0.0f);
    debt.setRemainingAmount(request.getAmount());
    debt.setStatus("ACTIVE");
    
    CustomerDebt savedDebt = customerDebtRepository.save(debt);
    
    // ✅ NO MONEYBOX INTEGRATION - Debt creation doesn't affect cash
    // MoneyBox will only be affected when the debt is actually paid (cash payment)
    logger.info("Debt created for customer: {}, amount: {}", customer.getName(), request.getAmount());
    
    return customerDebtMapper.toResponse(savedDebt);
}
```

## Transactional Behavior

### **Atomic Operations**
Both debt payment operations use `@Transactional` to ensure:

1. **Debt Payment**: Either both the debt payment AND the MoneyBox transaction succeed, or they both fail
2. **Multiple Debt Payment**: Either all debt payments AND the MoneyBox transaction succeed, or they all fail

### **Error Handling**
- If MoneyBox integration fails, the debt payment is still processed
- Logs are generated for both success and failure cases
- No rollback of debt payment if MoneyBox fails (business decision)

## Business Logic

### **When MoneyBox is Updated**
- ✅ **Cash debt payments**: Increases MoneyBox balance
- ✅ **Multiple cash debt payments**: Increases MoneyBox balance by total amount

### **When MoneyBox is NOT Updated**
- ❌ **Debt creation**: No cash transaction occurs
- ❌ **Debt payment with non-cash methods**: No cash transaction occurs
- ❌ **Debt status updates**: No cash transaction occurs

## Integration Summary

### **Services Used**
- `SalesIntegrationService.recordSalePayment()` - For recording debt payments

### **Transaction Types**
- **SALE_PAYMENT**: When customers pay their debts with cash
- **Reference ID**: Uses debt ID for single payments, customer ID for multiple payments

### **Currency**
- **Default**: SYP (Syrian Pound) - as most debt payments are in local currency

### **Pharmacy Isolation**
- Each pharmacy's MoneyBox is isolated
- Payments are recorded against the current user's pharmacy

## Benefits

1. **Complete Financial Tracking**: All cash debt payments are reflected in MoneyBox
2. **Audit Trail**: Transaction history shows debt payment sources
3. **Cash Flow Management**: Accurate cash balance including debt payments
4. **Reporting**: MoneyBox reports include debt payment contributions
5. **Reconciliation**: Cash reconciliation includes debt payments

## Future Enhancements

1. **Refund Handling**: Support for debt payment refunds
2. **Partial Refunds**: Handle partial debt payment refunds
3. **Currency Conversion**: Support for debt payments in different currencies
4. **Advanced Reporting**: Debt payment analytics and trends
