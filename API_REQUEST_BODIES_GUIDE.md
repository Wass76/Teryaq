# API Request Bodies Guide - Teryaq Pharmacy System

This guide provides comprehensive examples of request bodies for creating purchase orders, purchase invoices, and sales invoices in the Teryaq pharmacy management system.

---

## 📋 Table of Contents

1. [Purchase Order](#purchase-order)
2. [Purchase Invoice](#purchase-invoice)
3. [Sale Invoice](#sale-invoice)
4. [Field Descriptions](#field-descriptions)
5. [Validation Rules](#validation-rules)
6. [Common Error Scenarios](#common-error-scenarios)
7. [Testing Examples](#testing-examples)

---

## 🛒 Purchase Order

**Endpoint:** `POST /api/v1/purchase-orders`

**Description:** Creates a new purchase order for products from suppliers.

### Request Body Structure

```json
{
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "barcode": "1234567890123",
      "productType": "MASTER"
    }
  ]
}
```

### Complete Example with Multiple Items

```json
{
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "barcode": "1234567890123",
      "productType": "MASTER"
    },
    {
      "productId": 2,
      "quantity": 50,
      "price": 12.75,
      "barcode": "9876543210987",
      "productType": "PHARMACY"
    },
    {
      "productId": 3,
      "quantity": 200,
      "price": 3.25,
      "barcode": "5556667778889",
      "productType": "MASTER"
    }
  ]
}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `language` | String | No | "ar" | Language code (ar/en) |

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 🧾 Purchase Invoice

**Endpoint:** `POST /api/v1/purchase-invoices`

**Description:** Creates a purchase invoice to record received products from suppliers.

### Request Body Structure

```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    }
  ]
}
```

### Complete Example with Multiple Items

```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 1050.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "BANK_ACCOUNT",
  "items": [
    {
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    },
    {
      "productId": 2,
      "receivedQty": 50,
      "bonusQty": 5,
      "invoicePrice": 12.75,
      "batchNo": "BATCH002",
      "expiryDate": "2026-06-30",
      "productType": "PHARMACY",
      "sellingPrice": 18.00,
      "minStockLevel": 15
    },
    {
      "productId": 3,
      "receivedQty": 200,
      "bonusQty": 20,
      "invoicePrice": 3.25,
      "batchNo": "BATCH003",
      "expiryDate": "2025-08-15",
      "productType": "MASTER",
      "sellingPrice": 5.00,
      "minStockLevel": 25
    }
  ]
}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `language` | String | No | "ar" | Language code (ar/en) |

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 💰 Sale Invoice

**Endpoint:** `POST /api/v1/sales`

**Description:** Creates a sale invoice for products sold to customers.

### Request Body Structure

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 10.0,
  "paidAmount": null,
  "debtDueDate": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0
    }
  ]
}
```

### Complete Example with Multiple Items

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 15.0,
  "paidAmount": null,
  "debtDueDate": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0
    },
    {
      "stockItemId": 2,
      "quantity": 1,
      "unitPrice": 1200.0
    },
    {
      "stockItemId": 3,
      "quantity": 3,
      "unitPrice": 450.0
    }
  ]
}
```

### Credit Sale Example

```json
{
  "customerId": 1,
  "paymentType": "CREDIT",
  "paymentMethod": "BANK_ACCOUNT",
  "currency": "USD",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 50.0,
  "paidAmount": 200.0,
  "debtDueDate": "2024-12-31",
  "items": [
    {
      "stockItemId": 1,
      "quantity": 3,
      "unitPrice": 100.0
    }
  ]
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 📝 Field Descriptions

### Purchase Order Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `supplierId` | Long | Yes | ID of the supplier | 1 |
| `currency` | Currency | Yes | Currency for the order | "USD", "EUR", "SYP" |
| `items` | List | Yes | List of products to purchase | Array of items |

#### Purchase Order Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `productId` | Long | Yes | ID of the product | 1 |
| `quantity` | Integer | Yes | Quantity to purchase | 100 |
| `price` | Double | Yes | Unit price | 5.50 |
| `barcode` | String | No | Product barcode | "1234567890123" |
| `productType` | ProductType | Yes | Type of product | "MASTER", "PHARMACY" |

### Purchase Invoice Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `purchaseOrderId` | Long | Yes | ID of the purchase order | 1 |
| `supplierId` | Long | Yes | ID of the supplier | 1 |
| `currency` | Currency | Yes | Currency for the invoice | "USD", "EUR" |
| `total` | Double | Yes | Total invoice amount | 550.00 |
| `invoiceNumber` | String | No | Invoice number | "INV-2024-001" |
| `paymentMethod` | PaymentMethod | No | Payment method (defaults to CASH) | "CASH", "BANK_ACCOUNT", "CHECK" |
| `items` | List | Yes | List of received items | Array of items |

#### Purchase Invoice Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `productId` | Long | Yes | ID of the product | 1 |
| `receivedQty` | Integer | Yes | Quantity received | 100 |
| `bonusQty` | Integer | No | Bonus quantity | 10 |
| `invoicePrice` | Double | Yes | Invoice price per unit | 5.50 |
| `batchNo` | String | Yes | Batch number | "BATCH001" |
| `expiryDate` | LocalDate | Yes | Expiry date (YYYY-MM-DD) | "2025-12-31" |
| `productType` | ProductType | Yes | Type of product | "MASTER", "PHARMACY" |
| `sellingPrice` | Double | Conditional | Selling price (required for PHARMACY) | 8.50 |
| `minStockLevel` | Integer | No | Minimum stock level | 10 |

### Sale Invoice Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `customerId` | Long | Yes | ID of the customer | 1 |
| `paymentType` | PaymentType | Yes | Type of payment | "CASH", "CREDIT" |
| `paymentMethod` | PaymentMethod | Yes | Method of payment | "CASH", "BANK_ACCOUNT" |
| `currency` | Currency | No | Currency (defaults to SYP) | "SYP", "USD" |
| `invoiceDiscountType` | DiscountType | No | Type of discount | "PERCENTAGE", "FIXED_AMOUNT" |
| `invoiceDiscountValue` | Float | No | Value of discount | 10.0 (%), 50.0 (fixed) |
| `paidAmount` | Float | No | Amount paid (null for auto-calculate) | null |
| `debtDueDate` | LocalDate | Conditional | Due date for credit sales | "2024-12-31" |
| `items` | List | Yes | List of items sold | Array of items |

#### Sale Invoice Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `stockItemId` | Long | Yes | ID of the stock item | 1 |
| `quantity` | Integer | Yes | Quantity sold (1-10000) | 2 |
| `unitPrice` | Float | No | Unit price (uses stock price if not provided) | 800.0 |

---

## ✅ Validation Rules

### Purchase Order Validation

- **supplierId**: Must exist in database
- **currency**: Must be one of: USD, EUR, GBP, SAR, AED, SYP
- **items**: Must contain at least one item
- **productId**: Must exist in database
- **quantity**: Must be greater than 0
- **price**: Must be greater than 0
- **productType**: Must be either "MASTER" or "PHARMACY"

### Purchase Invoice Validation

- **purchaseOrderId**: Must exist and be in PENDING status
- **supplierId**: Must match the purchase order supplier
- **currency**: Must be one of: USD, EUR, GBP, SAR, AED
- **total**: Should match the calculated total from items
- **items**: Must contain at least one item
- **receivedQty**: Must be greater than 0
- **bonusQty**: Must be 0 or greater
- **invoicePrice**: Must be greater than 0
- **expiryDate**: Must be a future date
- **sellingPrice**: Required for PHARMACY products, optional for MASTER products
- **minStockLevel**: Must be 0 or greater

### Sale Invoice Validation

- **customerId**: Must exist in database
- **paymentType**: Must be either "CASH" or "CREDIT"
- **paymentMethod**: Must be either "CASH" or "BANK_ACCOUNT"
- **currency**: Must be either "SYP" or "USD"
- **invoiceDiscountValue**: 
  - For PERCENTAGE: Must be 0-100
  - For FIXED_AMOUNT: Must be 0 or greater
- **debtDueDate**: Required when paymentType is "CREDIT"
- **items**: Must contain at least one item
- **stockItemId**: Must exist and have sufficient stock
- **quantity**: Must be between 1 and 10000

---

## 🚨 Common Error Scenarios

### HTTP Status Codes

| Status | Description | Common Causes |
|--------|-------------|---------------|
| `200` | Success | Request processed successfully |
| `400` | Bad Request | Invalid data, validation errors |
| `401` | Unauthorized | Missing or invalid JWT token |
| `403` | Forbidden | Insufficient permissions |
| `404` | Not Found | Resource not found |
| `409` | Conflict | Business rule violation |
| `500` | Internal Server Error | Server-side error |

### Purchase Order Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Invalid supplier ID | 400 | Supplier doesn't exist | Use valid supplier ID |
| Empty items list | 400 | No items provided | Add at least one item |
| Invalid product type | 400 | Product type not recognized | Use "MASTER" or "PHARMACY" |
| Negative quantity | 400 | Quantity less than 1 | Use positive quantity |
| Negative price | 400 | Price less than 0 | Use positive price |

### Purchase Invoice Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Purchase order not found | 400 | Invalid purchase order ID | Use valid purchase order ID |
| Order not in PENDING status | 409 | Order already processed | Use order with PENDING status |
| Supplier mismatch | 400 | Supplier doesn't match order | Use correct supplier ID |
| Invalid expiry date | 400 | Past expiry date | Use future date |
| Empty items list | 400 | No items provided | Add at least one item |

### Sale Invoice Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Customer not found | 400 | Invalid customer ID | Use valid customer ID |
| Insufficient stock | 409 | Not enough stock available | Reduce quantity or choose different item |
| Invalid discount value | 400 | Discount exceeds limits | Use valid discount value |
| Missing debt due date | 400 | Credit sale without due date | Add debt due date for credit sales |
| Invalid payment amount | 400 | Payment amount validation failed | Use valid payment amount |

---

## 🧪 Testing Examples

### cURL Examples

#### 1. Create Purchase Order
```bash
curl -X POST "http://localhost:8080/api/v1/purchase-orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "supplierId": 1,
    "currency": "USD",
    "items": [
      {
        "productId": 1,
        "quantity": 100,
        "price": 5.50,
        "barcode": "1234567890123",
        "productType": "MASTER"
      }
    ]
  }'
```

#### 2. Create Purchase Invoice
```bash
curl -X POST "http://localhost:8080/api/v1/purchase-invoices" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "purchaseOrderId": 1,
    "supplierId": 1,
    "currency": "USD",
    "total": 550.00,
    "invoiceNumber": "INV-2024-001",
    "paymentMethod": "CASH",
    "items": [
      {
        "productId": 1,
        "receivedQty": 100,
        "bonusQty": 10,
        "invoicePrice": 5.50,
        "batchNo": "BATCH001",
        "expiryDate": "2025-12-31",
        "productType": "MASTER",
        "sellingPrice": 8.50,
        "minStockLevel": 10
      }
    ]
  }'
```

#### 3. Create Sale Invoice
```bash
curl -X POST "http://localhost:8080/api/v1/sales" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "paymentType": "CASH",
    "paymentMethod": "CASH",
    "currency": "SYP",
    "invoiceDiscountType": "PERCENTAGE",
    "invoiceDiscountValue": 10.0,
    "paidAmount": null,
    "debtDueDate": null,
    "items": [
      {
        "stockItemId": 1,
        "quantity": 2,
        "unitPrice": 800.0
      }
    ]
  }'
```

### Postman Examples

#### Environment Variables
```json
{
  "base_url": "http://localhost:8080",
  "jwt_token": "YOUR_JWT_TOKEN_HERE",
  "supplier_id": "1",
  "customer_id": "1",
  "product_id": "1",
  "stock_item_id": "1"
}
```

#### Collection Variables
```json
{
  "supplierId": "{{supplier_id}}",
  "customerId": "{{customer_id}}",
  "productId": "{{product_id}}",
  "stockItemId": "{{stock_item_id}}"
}
```

---

## 🔐 Authentication & Permissions

### Required JWT Token
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Required Roles
- **Purchase Orders**: PHARMACY_MANAGER or EMPLOYEE
- **Purchase Invoices**: PHARMACY_MANAGER or EMPLOYEE
- **Sales**: PHARMACY_MANAGER

### Pharmacy Scoping
- All operations are automatically scoped to the current user's pharmacy
- Users cannot access data from other pharmacies

---

## 📊 Response Examples

### Successful Purchase Order Response
```json
{
  "id": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "productType": "MASTER"
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Successful Purchase Invoice Response
```json
{
  "id": 1,
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

### Successful Sale Invoice Response
```json
{
  "id": 1,
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "totalAmount": 1440.00,
  "discount": 160.00,
  "paidAmount": 1440.00,
  "remainingAmount": 0.00,
  "items": [
    {
      "id": 1,
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0,
      "subTotal": 1600.0
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## 📚 Additional Resources

- **API Documentation**: Swagger UI available at `/swagger-ui.html`
- **Error Codes**: See response body for detailed error messages
- **Validation**: All requests are validated against business rules
- **Logging**: Check application logs for detailed error information

---

## 🆘 Support

For technical support or questions about the API:
- Check the application logs
- Review the Swagger documentation
- Contact the development team

---

*Last updated: January 2024*
*Version: 1.0*
