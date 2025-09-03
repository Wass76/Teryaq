# 📊 Reports API Documentation

## Overview
The Reports API provides comprehensive reporting capabilities for the Pharmacy Management System, implementing all 5 report categories from SRS requirements:
- **Sales Reports** (تقارير المبيعات)
- **Profit Reports** (تقارير الأرباح)
- **Inventory Reports** (تقارير المخزون)
- **Debt Reports** (تقارير الديون)
- **Purchase Reports** (تقارير الشراء)

## 🔄 Two Endpoint Versions

The Reports API provides **two versions** of each endpoint:

### **1. Original Endpoints** (with `pharmacyId` parameter)
- **Use Case**: Admin access, multi-tenant management, cross-pharmacy reporting
- **Path Pattern**: `/api/v1/reports/{category}/{type}`
- **Required**: `pharmacyId` parameter
- **Example**: `/api/v1/reports/sales/daily?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31`

### **2. Auto-Pharmacy Endpoints** (no `pharmacyId` parameter)
- **Use Case**: Regular pharmacy users, current user's pharmacy only
- **Path Pattern**: `/api/v1/reports/my/{category}/{type}`
- **Required**: None (auto-extracts from current user)
- **Example**: `/api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31`

---

## 🔧 Main Report Generation Endpoint

### **POST `/api/v1/reports/generate`**
**Purpose**: Universal endpoint to generate any type of report using a flexible request structure
- **Use Case**: When you need programmatic report generation with complex parameters
- **Request**: JSON body with `ReportRequest` object
- **Response**: `ReportResponse` with structured data
- **Auto-Pharmacy**: If `pharmacyId` is not provided, it auto-extracts from current user

---

## 📈 3.5.1 Sales Reports (تقارير المبيعات)

### **Original Endpoints**

#### **GET `/api/v1/reports/sales/daily`**
**Purpose**: Generate daily sales summary showing total sales, invoice count, and best-selling products
**Business Need**: Daily performance tracking and identifying top products

#### Request Parameters:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `pharmacyId` | String | ✅ | - | Pharmacy identifier |
| `startDate` | String | ✅ | - | Start date (YYYY-MM-DD) |
| `endDate` | String | ✅ | - | End date (YYYY-MM-DD) |
| `currency` | String | ❌ | SYP | Currency code |
| `language` | String | ❌ | EN | Language |

#### **GET `/api/v1/reports/sales/monthly`**
**Purpose**: Generate monthly sales report with comparisons and trend analysis
**Business Need**: Monthly performance analysis and trend identification

### **Auto-Pharmacy Endpoints**

#### **GET `/api/v1/reports/my/sales/daily`**
**Purpose**: Generate daily sales summary for current user's pharmacy
**Business Need**: Daily performance tracking for logged-in user's pharmacy

#### Request Parameters:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `startDate` | String | ✅ | - | Start date (YYYY-MM-DD) |
| `endDate` | String | ✅ | - | End date (YYYY-MM-DD) |
| `currency` | String | ❌ | SYP | Currency code |
| `language` | String | ❌ | EN | Language |

#### **GET `/api/v1/reports/my/sales/monthly`**
**Purpose**: Generate monthly sales report for current user's pharmacy
**Business Need**: Monthly performance analysis for logged-in user's pharmacy

#### Response Structure (Same for both versions):
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 25,
      "totalAmount": 150000.0,
      "currency": "SYP",
      "period": "2024-01-01 to 2024-01-31",
      "reportName": "Daily Sales Summary",
      "reportNameAr": "تقارير المبيعات اليومية"
    },
    "details": [
      {
        "id": "1",
        "date": "2024-01-01",
        "amount": 7500.0,
        "description": "Paracetamol 500mg",
        "descriptionAr": "Paracetamol 500mg",
        "additionalData": {
          "quantity": 150
        }
      }
    ]
  },
  "metadata": {
    "generatedAt": "2024-01-15T10:30:00",
    "reportType": "DAILY_SALES_SUMMARY",
    "pharmacyId": "1",
    "language": "EN"
  }
}
```

---

## 💰 3.5.2 Profit Reports (تقارير الأرباح)

### **Original Endpoints**

#### **GET `/api/v1/reports/profit/daily`**
#### **GET `/api/v1/reports/profit/monthly`**

### **Auto-Pharmacy Endpoints**

#### **GET `/api/v1/reports/my/profit/daily`**
#### **GET `/api/v1/reports/my/profit/monthly`**

#### Request Parameters: Same as sales reports (without pharmacyId)

#### Response Structure:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 25,
      "totalAmount": 45000.0,
      "currency": "SYP",
      "period": "2024-01-01 to 2024-01-31",
      "reportName": "Daily Profit Summary",
      "reportNameAr": "تقارير الأرباح اليومية"
    },
    "details": [
      {
        "id": "1",
        "date": "2024-01-01",
        "amount": 2500.0,
        "description": "Amoxicillin 250mg",
        "descriptionAr": "Amoxicillin 250mg",
        "additionalData": {
          "quantity": 100,
          "profitMargin": 33.33
        }
      }
    ]
  }
}
```

---

## 📦 3.5.3 Inventory Reports (تقارير المخزون)

### **Original Endpoints**

#### **GET `/api/v1/reports/inventory/current`**
#### **GET `/api/v1/reports/inventory/movement`**

### **Auto-Pharmacy Endpoints**

#### **GET `/api/v1/reports/my/inventory/current`**
#### **GET `/api/v1/reports/my/inventory/movement`**

#### Request Parameters (Current Inventory):
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `currency` | String | ❌ | SYP | Currency code |
| `language` | String | ❌ | EN | Language |

#### Request Parameters (Movement - with date range):
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `startDate` | String | ✅ | - | Start date (YYYY-MM-DD) |
| `endDate` | String | ✅ | - | End date (YYYY-MM-DD) |
| `currency` | String | ❌ | SYP | Currency code |
| `language` | String | ❌ | EN | Language |

#### Response Structure:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 150,
      "totalAmount": 250000.0,
      "currency": "SYP",
      "period": "Current Inventory",
      "reportName": "Current Inventory",
      "reportNameAr": "تقارير المخزون الحالي"
    },
    "details": [
      {
        "id": "low-Paracetamol 500mg",
        "date": "Current",
        "amount": 5.0,
        "description": "Low Stock: Paracetamol 500mg",
        "descriptionAr": "مخزون منخفض: Paracetamol 500mg",
        "additionalData": {
          "minStockLevel": 10,
          "batchNo": "BATCH001"
        }
      },
      {
        "id": "exp-Aspirin 100mg",
        "date": "Expiring",
        "amount": 50.0,
        "description": "Expiring: Aspirin 100mg",
        "descriptionAr": "منتهي الصلاحية: Aspirin 100mg",
        "additionalData": {
          "expiryDate": "2024-02-15",
          "batchNo": "BATCH002"
        }
      }
    ]
  }
}
```

---

## 💳 3.5.4 Debt Reports (تقارير الديون)

### **Original Endpoints**

#### **GET `/api/v1/reports/debt/summary`**

### **Auto-Pharmacy Endpoints**

#### **GET `/api/v1/reports/my/debt/summary`**

#### Request Parameters:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `currency` | String | ❌ | SYP | Currency code |
| `language` | String | ❌ | EN | Language |

#### Response Structure:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 45,
      "totalAmount": 750000.0,
      "currency": "SYP",
      "period": "Current Debt Status",
      "reportName": "Customer Debt Summary",
      "reportNameAr": "تقارير ديون العملاء"
    },
    "details": [
      {
        "id": "customer-Ahmed Ali",
        "date": "Current",
        "amount": 50000.0,
        "description": "Ahmed Ali",
        "descriptionAr": "Ahmed Ali",
        "additionalData": {
          "phone": "+963912345678",
          "debtCount": 3
        }
      },
      {
        "id": "overdue-Sara Hassan",
        "date": "Overdue",
        "amount": 25000.0,
        "description": "Overdue: Sara Hassan",
        "descriptionAr": "متأخر: Sara Hassan",
        "additionalData": {
          "phone": "+963987654321",
          "dueDate": "2024-01-10",
          "notes": "Payment reminder sent"
        }
      }
    ]
  }
}
```

---

## 🛒 3.5.5 Purchase Reports (تقارير الشراء)

### **Original Endpoints**

#### **GET `/api/v1/reports/purchase/daily`**
#### **GET `/api/v1/reports/purchase/monthly`**

### **Auto-Pharmacy Endpoints**

#### **GET `/api/v1/reports/my/purchase/daily`**
#### **GET `/api/v1/reports/my/purchase/monthly`**

#### Request Parameters: Same as sales reports (without pharmacyId)

#### Response Structure:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": 8,
      "totalAmount": 120000.0,
      "currency": "SYP",
      "period": "2024-01-01 to 2024-01-31",
      "reportName": "Daily Purchase Summary",
      "reportNameAr": "تقارير الشراء اليومية"
    }
  }
}
```

---

## 🧪 Test Endpoints (For Development)

### **GET `/api/v1/reports/test/status`**
**Purpose**: Health check for the reports service
- **Request Parameters**: `pharmacyId` (optional)
- **Response**: Service status confirmation

### **GET `/api/v1/reports/test/sales/daily`**
**Purpose**: Test daily sales database query directly
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw database query results

### **GET `/api/v1/reports/test/profit/daily`**
**Purpose**: Test daily profit database query directly
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw profit data

### **GET `/api/v1/reports/test/inventory/current`**
**Purpose**: Test current inventory query
- **Request Parameters**: `pharmacyId`
- **Response**: Raw inventory data

### **GET `/api/v1/reports/test/inventory/low-stock`**
**Purpose**: Test low stock products query
- **Request Parameters**: `pharmacyId`
- **Response**: Raw low stock data

### **GET `/api/v1/reports/test/inventory/expiring`**
**Purpose**: Test expiring products query
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw expiring products data

### **GET `/api/v1/reports/test/inventory/movement`**
**Purpose**: Test inventory movement query
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw movement data

### **GET `/api/v1/reports/test/debt/summary`**
**Purpose**: Test debt summary query
- **Request Parameters**: `pharmacyId`
- **Response**: Raw debt data

### **GET `/api/v1/reports/test/debt/most-indebted`**
**Purpose**: Test most indebted customers query
- **Request Parameters**: `pharmacyId`
- **Response**: Raw customer debt data

### **GET `/api/v1/reports/test/debt/overdue`**
**Purpose**: Test overdue debts query
- **Request Parameters**: `pharmacyId`
- **Response**: Raw overdue debt data

### **GET `/api/v1/reports/test/purchase/daily`**
**Purpose**: Test daily purchase query
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw purchase data

### **GET `/api/v1/reports/test/purchase/by-supplier`**
**Purpose**: Test purchase by supplier query
- **Request Parameters**: `pharmacyId`, `startDate`, `endDate`
- **Response**: Raw supplier purchase data

---

## 🎯 Key Business Purposes

### **Sales Reports**
- **Daily Tracking**: Monitor daily sales performance
- **Product Analysis**: Identify best-selling products
- **Trend Analysis**: Track sales patterns over time
- **Performance Metrics**: Track total sales, invoice count, and revenue

### **Profit Reports**
- **Profitability Analysis**: Monitor profit margins
- **Product Performance**: Identify most profitable products
- **Financial Planning**: Support business decision making
- **Revenue Analysis**: Track revenue vs profit trends

### **Inventory Reports**
- **Stock Management**: Monitor current stock levels
- **Alert System**: Identify low stock and expiring products
- **Optimization**: Analyze product movement patterns
- **Value Tracking**: Monitor inventory total value

### **Debt Reports**
- **Credit Management**: Track customer debts
- **Collection Planning**: Identify overdue payments
- **Risk Assessment**: Monitor credit exposure
- **Customer Analysis**: Identify most indebted customers

### **Purchase Reports**
- **Procurement Tracking**: Monitor purchase activities
- **Cost Analysis**: Track procurement costs
- **Supplier Management**: Analyze supplier performance
- **Budget Planning**: Support procurement planning

---

## 🔄 Response Structure Consistency

All endpoints follow a consistent response structure:

### **Success Response**
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalRecords": number,
      "totalAmount": number,
      "currency": string,
      "period": string,
      "reportName": string,
      "reportNameAr": string
    },
    "details": [
      {
        "id": string,
        "date": string,
        "amount": number,
        "description": string,
        "descriptionAr": string,
        "additionalData": object
      }
    ]
  },
  "metadata": {
    "generatedAt": string,
    "reportType": string,
    "pharmacyId": string,
    "language": string
  }
}
```

### **Error Response**
```json
{
  "success": false,
  "error": "Error message",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 📋 Common Request Parameters

### **Date Format**
All date parameters should be in `YYYY-MM-DD` format:
- Example: `2024-01-15`

### **Currency Options**
- `SYP` - Syrian Pound (default)
- `USD` - US Dollar
- `EUR` - Euro

### **Language Options**
- `EN` - English (default)
- `AR` - Arabic

### **Pharmacy ID**
- String format representing the pharmacy identifier
- Example: `"1"`, `"pharmacy_001"`
- **Note**: Only required for original endpoints, auto-extracted for `/my/` endpoints

---

## 🚀 Usage Examples

### **Original Endpoints (with pharmacyId)**

#### **Daily Sales Report**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/sales/daily?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN"
```

#### **Current Inventory Report**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/inventory/current?pharmacyId=1&currency=SYP&language=AR"
```

#### **Customer Debt Report**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/debt/summary?pharmacyId=1&currency=SYP&language=EN"
```

### **Auto-Pharmacy Endpoints (no pharmacyId)**

#### **Daily Sales Report (My Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN"
```

#### **Current Inventory Report (My Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/my/inventory/current?currency=SYP&language=AR"
```

#### **Customer Debt Report (My Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/my/debt/summary?currency=SYP&language=EN"
```

### **Test Endpoint**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/test/status?pharmacyId=1"
```

---

## 🔧 Technical Notes

### **Auto-Pharmacy Implementation**
- **Service Layer**: `ReportService` extends `BaseSecurityService`
- **Method**: `getCurrentUserPharmacyId()` automatically extracts pharmacy ID from authenticated user
- **Security**: Validates user authentication and pharmacy association
- **Fallback**: Original endpoints still work with explicit pharmacyId parameter

### **Database Queries**
- All reports use optimized JPQL queries
- Queries are filtered by pharmacy ID for multi-tenant support
- Date ranges are inclusive of start and end dates
- Results are ordered by relevance (e.g., highest sales first)

### **Performance Considerations**
- Large date ranges may impact performance
- Consider using pagination for detailed reports
- Database indexes are optimized for common query patterns

### **Error Handling**
- Invalid date formats return 400 Bad Request
- Missing required parameters return 400 Bad Request
- Database errors return 500 Internal Server Error
- Authentication errors return 401 Unauthorized
- All errors include meaningful error messages

### **Caching**
- Reports can be cached for better performance
- Cache invalidation occurs when data changes
- Consider implementing Redis for report caching

---

## 📊 Report Categories Summary

| Category | Original Endpoints | Auto-Pharmacy Endpoints | Purpose | Frequency |
|----------|-------------------|-------------------------|---------|-----------|
| **Sales** | 2 | 2 | Track sales performance | Daily/Monthly |
| **Profit** | 2 | 2 | Monitor profitability | Daily/Monthly |
| **Inventory** | 2 | 2 | Manage stock levels | Real-time/Periodic |
| **Debt** | 1 | 1 | Track customer debts | Real-time |
| **Purchase** | 2 | 2 | Monitor procurement | Daily/Monthly |

## 🔐 Security & Access Control

### **Original Endpoints**
- **Access**: Admin users, cross-pharmacy management
- **Validation**: Explicit pharmacyId parameter required
- **Use Case**: Multi-tenant administration, system-wide reporting

### **Auto-Pharmacy Endpoints**
- **Access**: Authenticated pharmacy users only
- **Validation**: Automatic pharmacy ID extraction from current user
- **Use Case**: Regular pharmacy operations, user-specific reporting
- **Security**: Inherits from `BaseSecurityService` security model

This design ensures comprehensive reporting coverage for all aspects of pharmacy management while maintaining consistency, security, and ease of use for both admin and regular users.
