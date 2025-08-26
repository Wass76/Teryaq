# MoneyBox API Documentation

## Overview
The MoneyBox API provides endpoints for managing pharmacy cash money boxes, including creation, transaction recording, reconciliation, and reporting.

**Base URL**: `/api/v1/moneybox`

**Authentication**: Bearer Token Required

**Note**: All endpoints automatically use the current user's pharmacy ID from the authentication context.

---

## 📋 API Endpoints

### 1. Create Money Box

**Endpoint**: `POST /api/v1/moneybox`

**Description**: Creates a new money box for the current user's pharmacy with initial balance and currency.

**Request Body**:
```json
{
    "initialBalance": 1000.00,
    "currency": "SYP"
}
```

**Response (201 Created)**:
```json
{
    "id": 1,
    "pharmacyId": 1,
    "currentBalance": 1000.00,
    "initialBalance": 1000.00,
    "lastReconciled": null,
    "reconciledBalance": null,
    "status": "OPEN",
    "currency": "SYP",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400` - Invalid request data
- `409` - Pharmacy already has a money box
- `500` - Internal server error

---

### 2. Get Money Box for Current Pharmacy

**Endpoint**: `GET /api/v1/moneybox`

**Description**: Retrieves the money box information for the current user's pharmacy.

**Response (200 OK)**:
```json
{
    "id": 1,
    "pharmacyId": 1,
    "currentBalance": 1250.75,
    "initialBalance": 1000.00,
    "lastReconciled": "2024-01-15T20:00:00",
    "reconciledBalance": 1250.75,
    "status": "OPEN",
    "currency": "SYP",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T20:00:00"
}
```

**Error Responses**:
- `404` - Money box not found for the pharmacy
- `500` - Internal server error

---

### 3. Add Manual Transaction

**Endpoint**: `POST /api/v1/moneybox/transaction`

**Description**: Adds a manual transaction (income or expense) to the current pharmacy's money box.

**Query Parameters**:
- `amount` (BigDecimal, required) - Transaction amount (positive for income, negative for expense)
- `description` (String, optional) - Transaction description

**Example Requests**:
```bash
# Add income
POST /api/v1/moneybox/transaction?amount=100.50&description=Investment%20return

# Add expense
POST /api/v1/moneybox/transaction?amount=-25.00&description=Office%20supplies
```

**Response (200 OK)**:
```json
{
    "id": 1,
    "pharmacyId": 1,
    "currentBalance": 1326.25,  // Updated balance
    "initialBalance": 1000.00,
    "lastReconciled": "2024-01-15T20:00:00",
    "reconciledBalance": 1250.75,
    "status": "OPEN",
    "currency": "SYP",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T21:15:00"
}
```

**Error Responses**:
- `400` - Invalid amount
- `404` - Money box not found
- `409` - Money box is not open for transactions
- `500` - Internal server error

---

### 4. Reconcile Cash

**Endpoint**: `POST /api/v1/moneybox/reconcile`

**Description**: Reconciles the current pharmacy's money box balance with actual physical cash count.

**Query Parameters**:
- `actualCashCount` (BigDecimal, required) - Actual physical cash count amount
- `notes` (String, optional) - Reconciliation notes

**Example Request**:
```bash
POST /api/v1/moneybox/reconcile?actualCashCount=1320.00&notes=Daily%20end-of-shift%20count
```

**Response (200 OK)**:
```json
{
    "id": 1,
    "pharmacyId": 1,
    "currentBalance": 1320.00,  // Adjusted to match actual count
    "initialBalance": 1000.00,
    "lastReconciled": "2024-01-15T22:00:00",  // Updated timestamp
    "reconciledBalance": 1320.00,  // Updated reconciled balance
    "status": "OPEN",
    "currency": "SYP",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T22:00:00"
}
```

**Error Responses**:
- `400` - Invalid cash count amount
- `404` - Money box not found
- `500` - Internal server error

---

### 5. Get Period Summary

**Endpoint**: `GET /api/v1/moneybox/summary`

**Description**: Retrieves a summary of transactions for the specified period for the current pharmacy.

**Query Parameters**:
- `startDate` (LocalDateTime, required) - Start date and time for the period
- `endDate` (LocalDateTime, required) - End date and time for the period

**Example Request**:
```bash
GET /api/v1/moneybox/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

**Response (200 OK)**:
```json
{
    "totalIncome": 5000.00,
    "totalExpense": 2500.00,
    "netAmount": 2500.00,
    "periodStart": "2024-01-01T00:00:00",
    "periodEnd": "2024-01-31T23:59:59"
}
```

**Error Responses**:
- `400` - Invalid date range
- `404` - Money box not found
- `500` - Internal server error

---

## 📊 Data Models

### MoneyBoxRequestDTO
```json
{
    "initialBalance": "BigDecimal (required, min: 0.0)",
    "currency": "String (required, length: 3)"
}
```

### MoneyBoxResponseDTO
```json
{
    "id": "Long",
    "pharmacyId": "Long",
    "currentBalance": "BigDecimal",
    "initialBalance": "BigDecimal",
    "lastReconciled": "LocalDateTime",
    "reconciledBalance": "BigDecimal",
    "status": "MoneyBoxStatus (OPEN/CLOSED/SUSPENDED/PENDING)",
    "currency": "String",
    "createdAt": "LocalDateTime",
    "updatedAt": "LocalDateTime"
}
```

### MoneyBoxSummary
```json
{
    "totalIncome": "BigDecimal",
    "totalExpense": "BigDecimal",
    "netAmount": "BigDecimal",
    "periodStart": "LocalDateTime",
    "periodEnd": "LocalDateTime"
}
```

---

## 🔧 Integration Information

### Automatic Integration
The MoneyBox automatically integrates with:
- **Sales**: Cash payments and refunds
- **Purchases**: Cash payments and refunds  
- **Customer Debts**: Cash debt payments

### Transaction Types
- `SALE_PAYMENT` - Cash sales
- `SALE_REFUND` - Cash sale refunds
- `PURCHASE_PAYMENT` - Cash purchases
- `PURCHASE_REFUND` - Cash purchase refunds
- `DEBT_PAYMENT` - Cash debt payments
- `EXPENSE` - Manual expenses
- `INCOME` - Manual income
- `ADJUSTMENT` - Reconciliation adjustments

---

## 🚨 Error Handling

### Common Error Codes
- `400` - Bad Request (validation errors)
- `404` - Not Found (resource doesn't exist)
- `409` - Conflict (business rule violations)
- `500` - Internal Server Error

### Validation Rules
- Initial balance must be >= 0
- Currency must be exactly 3 characters
- Amount for transactions must not be 0
- Actual cash count must be >= 0
- Date ranges must be valid

---

## 🔐 Security

### Authentication
All endpoints require Bearer token authentication.

### Authorization
- Users can only access money boxes for their pharmacy
- Pharmacy ID is automatically extracted from user context
- Role-based access control applies
- All operations are logged for audit purposes

### Data Protection
- Pharmacy isolation ensures data privacy
- All transactions are encrypted in transit
- Audit logs track all changes

---

## 📈 Usage Examples

### Complete Workflow Example

1. **Create Money Box**:
```bash
POST /api/v1/moneybox
{
    "initialBalance": 1000.00,
    "currency": "SYP"
}
```

2. **Check Balance**:
```bash
GET /api/v1/moneybox
```

3. **Record Manual Transaction**:
```bash
POST /api/v1/moneybox/transaction?amount=50.00&description=Deposit
```

4. **Daily Reconciliation**:
```bash
POST /api/v1/moneybox/reconcile?actualCashCount=1050.00&notes=End%20of%20day
```

5. **Monthly Summary**:
```bash
GET /api/v1/moneybox/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

---

## 🔍 Best Practices

### API Usage
1. Always validate responses and handle errors
2. Use appropriate HTTP status codes
3. Include meaningful error messages
4. Implement proper retry logic for transient failures

### Data Management
1. Reconcile cash daily
2. Review transaction logs regularly
3. Maintain backup of important data
4. Monitor for unusual activity

### Integration
1. Test integration endpoints thoroughly
2. Handle integration failures gracefully
3. Monitor transaction volumes
4. Implement proper logging and monitoring

### Security
1. Always use Bearer token authentication
2. Pharmacy ID is automatically handled
3. No need to pass pharmacy ID in requests
4. All operations are isolated to current user's pharmacy
