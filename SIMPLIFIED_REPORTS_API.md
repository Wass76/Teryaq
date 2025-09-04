# 📊 Simplified Reports API Documentation

## Overview

After the business meeting, the reports feature has been simplified to include only the specific reports that were agreed upon:

### **Required Reports:**
1. **Monthly Purchase Report** (with daily breakdown)
2. **Daily Purchase Report**
3. **Monthly Profit Report** (with daily breakdown)
4. **Daily Profit Report**
5. **Most Sold Categories Monthly**
6. **Top 10 Products Monthly**

## 🚀 API Endpoints

### Base URL
```
http://localhost:8080/api/v1/reports
```

### 1. Monthly Purchase Report
**GET** `/purchase/monthly`

Returns purchase data for each day in the specified month.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `startDate` (required): Start date in YYYY-MM-DD format
- `endDate` (required): End date in YYYY-MM-DD format
- `currency` (optional): Currency code (default: SYP)
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "currency": "SYP",
  "language": "EN",
  "reportType": "monthly_purchase",
  "dailyData": [
    {
      "date": "2024-01-01",
      "totalInvoices": 5,
      "totalAmount": 15000.0,
      "totalPaid": 12000.0
    }
  ],
  "summary": {
    "totalInvoices": 150,
    "totalAmount": 450000.0,
    "totalPaid": 380000.0,
    "averageAmount": 3000.0
  }
}
```

### 2. Daily Purchase Report
**GET** `/purchase/daily`

Returns purchase data for a specific day.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `date` (required): Date in YYYY-MM-DD format
- `currency` (optional): Currency code (default: SYP)
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/purchase/daily?pharmacyId=1&date=2024-01-15&currency=SYP&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "date": "2024-01-15",
  "currency": "SYP",
  "language": "EN",
  "reportType": "daily_purchase",
  "data": {
    "totalInvoices": 8,
    "totalAmount": 25000.0,
    "totalPaid": 20000.0,
    "averageAmount": 3125.0
  },
  "items": [
    {
      "productName": "Paracetamol 500mg",
      "quantity": 100,
      "unitPrice": 50.0,
      "subTotal": 5000.0,
      "supplierName": "ABC Suppliers"
    }
  ]
}
```

### 3. Monthly Profit Report
**GET** `/profit/monthly`

Returns profit data for each day in the specified month.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `startDate` (required): Start date in YYYY-MM-DD format
- `endDate` (required): End date in YYYY-MM-DD format
- `currency` (optional): Currency code (default: SYP)
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/profit/monthly?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "currency": "SYP",
  "language": "EN",
  "reportType": "monthly_profit",
  "dailyData": [
    {
      "date": "2024-01-01",
      "totalInvoices": 12,
      "totalRevenue": 18000.0,
      "totalProfit": 4500.0
    }
  ],
  "summary": {
    "totalInvoices": 360,
    "totalRevenue": 540000.0,
    "totalProfit": 135000.0,
    "averageRevenue": 1500.0
  }
}
```

### 4. Daily Profit Report
**GET** `/profit/daily`

Returns profit data for a specific day.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `date` (required): Date in YYYY-MM-DD format
- `currency` (optional): Currency code (default: SYP)
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/profit/daily?pharmacyId=1&date=2024-01-15&currency=SYP&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "date": "2024-01-15",
  "currency": "SYP",
  "language": "EN",
  "reportType": "daily_profit",
  "data": {
    "totalInvoices": 15,
    "totalRevenue": 22500.0,
    "totalProfit": 5625.0,
    "averageRevenue": 1500.0
  },
  "items": [
    {
      "productName": "Paracetamol 500mg",
      "quantity": 20,
      "revenue": 2000.0,
      "profit": 500.0
    }
  ]
}
```

### 5. Most Sold Categories Monthly
**GET** `/categories/most-sold`

Returns the most sold categories in the pharmacy for the specified month.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `startDate` (required): Start date in YYYY-MM-DD format
- `endDate` (required): End date in YYYY-MM-DD format
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/categories/most-sold?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "language": "EN",
  "reportType": "most_sold_categories",
  "categories": [
    {
      "categoryName": "Pain Relief",
      "totalQuantity": 500,
      "totalRevenue": 25000.0,
      "invoiceCount": 45
    },
    {
      "categoryName": "Antibiotics",
      "totalQuantity": 300,
      "totalRevenue": 15000.0,
      "invoiceCount": 30
    }
  ]
}
```

### 6. Top 10 Products Monthly
**GET** `/products/top-10`

Returns the top 10 most sold products in the pharmacy for the specified month.

**Parameters:**
- `pharmacyId` (required): Pharmacy ID
- `startDate` (required): Start date in YYYY-MM-DD format
- `endDate` (required): End date in YYYY-MM-DD format
- `language` (optional): Language code (default: EN)

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/products/top-10?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&language=EN"
```

**Response:**
```json
{
  "success": true,
  "pharmacyId": 1,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "language": "EN",
  "reportType": "top_10_products",
  "products": [
    {
      "productName": "Paracetamol 500mg",
      "totalQuantity": 200,
      "totalRevenue": 10000.0,
      "invoiceCount": 25
    },
    {
      "productName": "Amoxicillin 500mg",
      "totalQuantity": 150,
      "totalRevenue": 7500.0,
      "invoiceCount": 20
    }
  ]
}
```

### 7. Health Check
**GET** `/health`

Check if the reports service is running.

**Example:**
```bash
curl "http://localhost:8080/api/v1/reports/health"
```

**Response:**
```json
{
  "status": "OK",
  "service": "Reports API"
}
```

## 📋 Data Format Requirements

### Date Format
All date parameters must be in **YYYY-MM-DD** format:
- ✅ `2024-01-01`
- ❌ `2024-01-01T00:00:00` (contains time)
- ❌ `01/01/2024` (wrong format)

### Currency Codes
- `SYP` - Syrian Pound (default)
- `USD` - US Dollar
- `EUR` - Euro

### Language Codes
- `EN` - English (default)
- `AR` - Arabic

## 🔧 Error Handling

All endpoints return consistent error responses:

**Success Response:**
```json
{
  "success": true,
  // ... other data
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Error message description"
}
```

## 🚀 Usage Examples

### Frontend JavaScript
```javascript
// Monthly Purchase Report
const getMonthlyPurchaseReport = async (pharmacyId, startDate, endDate) => {
  const response = await fetch(
    `/api/v1/reports/purchase/monthly?pharmacyId=${pharmacyId}&startDate=${startDate}&endDate=${endDate}`
  );
  return await response.json();
};

// Daily Profit Report
const getDailyProfitReport = async (pharmacyId, date) => {
  const response = await fetch(
    `/api/v1/reports/profit/daily?pharmacyId=${pharmacyId}&date=${date}`
  );
  return await response.json();
};
```

### cURL Examples
```bash
# Monthly Purchase Report
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31"

# Daily Profit Report
curl "http://localhost:8080/api/v1/reports/profit/daily?pharmacyId=1&date=2024-01-15"

# Most Sold Categories
curl "http://localhost:8080/api/v1/reports/categories/most-sold?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31"

# Top 10 Products
curl "http://localhost:8080/api/v1/reports/products/top-10?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31"
```

## 📊 Business Requirements Fulfilled

✅ **Monthly Purchase Report**: Returns purchase data with daily breakdown  
✅ **Daily Purchase Report**: Returns purchase data for specific day  
✅ **Monthly Profit Report**: Returns profit data with daily breakdown  
✅ **Daily Profit Report**: Returns profit data for specific day  
✅ **Most Sold Categories**: Returns most sold categories monthly  
✅ **Top 10 Products**: Returns top 10 most sold products monthly  

## 🔄 Changes Made

### Removed Features
- ❌ Sales reports (not required)
- ❌ Inventory reports (not required)
- ❌ Debt reports (not required)
- ❌ Complex DTOs and enums
- ❌ Auto-pharmacy endpoints
- ❌ Chart data generation
- ❌ Export functionality

### Simplified Architecture
- ✅ Direct Map<String, Object> responses
- ✅ Simple service methods
- ✅ Focused repository queries
- ✅ Clean controller endpoints
- ✅ Minimal dependencies

This simplified approach focuses on delivering exactly what the business team requested while maintaining clean, maintainable code.
