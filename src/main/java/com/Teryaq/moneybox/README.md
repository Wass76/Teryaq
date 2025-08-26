# MoneyBox Feature - Continuous Single Box Implementation

## Overview
The MoneyBox feature implements a **Continuous Single Box** approach for cash management in the pharmacy system. This follows the architectural decision documented in `MONEYBOX_ARCHITECTURAL_DECISION.md` which recommends starting simple and evolving based on business needs.

## Architecture Decision
Based on the architectural analysis, we implemented the **Continuous Single Box** approach because:
- ✅ **Simplicity**: Single source of truth for cash balance
- ✅ **Performance**: Fewer database queries and better performance  
- ✅ **Consistency**: No synchronization issues between multiple boxes
- ✅ **Maintainability**: Simpler codebase with fewer edge cases
- ✅ **Testing**: Easier to test and validate
- ✅ **Deployment**: Lower risk deployment with fewer moving parts

## Core Architecture

### Single Money Box Entity
```java
@Entity
public class MoneyBox {
    private Long id;
    private Long pharmacyId;           // One box per pharmacy
    private BigDecimal currentBalance; // Single balance tracking
    private BigDecimal initialBalance;
    private LocalDateTime lastReconciled;
    private BigDecimal reconciledBalance;
    private MoneyBoxStatus status;
    private String currency;
}
```

### Transaction-Based Approach
- All cash movements are recorded as transactions
- Single balance updated atomically with each transaction
- Period-based reporting generated from transaction history
- No complex transfer logic between multiple boxes

## Components

### Entities
- **MoneyBox**: Single entity per pharmacy with continuous operation
- **MoneyBoxTransaction**: Records all transactions (deposits, withdrawals, payments)
- **ExchangeRate**: Manages currency conversion rates

### DTOs
- **MoneyBoxRequestDTO**: For creating money boxes
- **MoneyBoxResponseDTO**: For returning money box data
- **TransactionRequestDTO**: For transaction operations

### Services
- **MoneyBoxService**: Core business logic with simple transaction operations
- **SalesIntegrationService**: Integration with sales operations
- **PurchaseIntegrationService**: Integration with purchase operations

### Key Methods
```java
// Simple transaction addition
addTransaction(pharmacyId, amount, description)

// Cash reconciliation
reconcileCash(pharmacyId, actualCashCount, notes)

// Period-based reporting
getPeriodSummary(pharmacyId, startDate, endDate)

// Sales integration
recordSalePayment(pharmacyId, saleId, amount, currency)
recordSaleRefund(pharmacyId, saleId, amount, currency)

// Purchase integration
recordPurchasePayment(pharmacyId, purchaseId, amount, currency)
recordPurchaseRefund(pharmacyId, purchaseId, amount, currency)

// Expense & Income
recordExpense(pharmacyId, description, amount, currency)
recordIncome(pharmacyId, description, amount, currency)
```

## API Endpoints

### Core Operations
- `POST /api/moneybox` - Create money box for pharmacy
- `GET /api/moneybox/pharmacy/{pharmacyId}` - Get pharmacy's money box
- `POST /api/moneybox/pharmacy/{pharmacyId}/transaction` - Add transaction
- `POST /api/moneybox/pharmacy/{pharmacyId}/reconcile` - Reconcile cash
- `GET /api/moneybox/pharmacy/{pharmacyId}/summary` - Get period summary

### Sales Integration
- `POST /api/moneybox/integration/sales/payment` - Record sale payment
- `POST /api/moneybox/integration/sales/refund` - Record sale refund
- `GET /api/moneybox/integration/sales/amount` - Get sales amount for period

### Purchase Integration
- `POST /api/moneybox/integration/purchases/payment` - Record purchase payment
- `POST /api/moneybox/integration/purchases/refund` - Record purchase refund
- `GET /api/moneybox/integration/purchases/amount` - Get purchase amount for period

### Expense & Income
- `POST /api/moneybox/integration/expenses` - Record expense
- `POST /api/moneybox/integration/income` - Record income

## Database Schema

### Single Money Box Table
```sql
CREATE TABLE money_box (
    id BIGINT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL,
    initial_balance DECIMAL(15,2) NOT NULL,
    last_reconciled TIMESTAMP,
    reconciled_balance DECIMAL(15,2),
    status VARCHAR(20),
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Transaction History
```sql
CREATE TABLE money_box_transaction (
    id BIGINT PRIMARY KEY,
    money_box_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP
);
```

## Business Benefits

### Phase 1: Core Functionality ✅
- Fast development (2-3 weeks)
- Reliable operation
- Easy maintenance
- Good performance
- Lower risk

### Phase 2: Enhanced Reporting (Future)
- Period-based reporting (daily, weekly, monthly, yearly)
- Rich analytics without data fragmentation
- Easy to implement and maintain

### Phase 3: Multi-Box Evolution (If Business Grows)
- Future enhancement based on actual business needs
- Gradual complexity increase
- Backward compatibility
- Business-driven evolution

## Key Features
- ✅ Single continuous money box per pharmacy
- ✅ Atomic transaction processing
- ✅ Cash reconciliation
- ✅ Period-based reporting capability
- ✅ Currency support
- ✅ Audit trail through transactions
- ✅ **Sales Integration**: Automatic recording of sale payments and refunds
- ✅ **Purchase Integration**: Automatic recording of purchase payments and refunds
- ✅ **Expense Management**: Record and track various expenses
- ✅ **Income Tracking**: Record additional income sources
- ✅ **Transaction Types**: Comprehensive transaction categorization

## Success Metrics
- **Performance**: API response time < 200ms
- **Reliability**: 99.9% uptime
- **Reconciliation Time**: < 15 minutes per day
- **User Adoption**: Simple interface for easy adoption

## Future Evolution
This implementation provides a solid foundation that can evolve based on business needs:
- Add multiple boxes if required by business growth
- Implement advanced reporting features
- Add transfer capabilities between boxes
- Extend with role-based access control

The architecture decision prioritizes **simplicity and reliability** over premature complexity, ensuring the system meets current needs while remaining adaptable to future requirements.
