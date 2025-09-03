# 🔍 Date Parameter Error Troubleshooting Guide

## Current Error
```
"message": "Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; 
Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam java.time.LocalDate] 
for value [2024-01-01T00:00:00]"
```

## 🔍 Where This Error Can Occur

### **1. Reports API Endpoints** ✅ FIXED
All Reports API endpoints now use `LocalDate` parameters and expect `YYYY-MM-DD` format:

```bash
# ✅ Correct format (works)
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31"

# ❌ Wrong format (causes error)
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-31"
```

### **2. Customer Debt Controller** ✅ FIXED
The `CustomerDebtController` had a conflicting annotation that was fixed:

```java
// ❌ Before (caused error)
@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate

// ✅ After (fixed)
@RequestParam LocalDate startDate
```

### **3. Other Controllers** ✅ CORRECT
These controllers use `LocalDateTime` or `String` parameters correctly:

- **MoneyBoxController**: Uses `LocalDateTime` (expects `2024-01-01T00:00:00`)
- **PurchaseInvoiceController**: Uses `String` with manual parsing (expects `2024-01-01T00:00:00`)
- **PurchaseOrderController**: Uses `String` with manual parsing (expects `2024-01-01T00:00:00`)

## 🚀 How to Identify the Source

### **Step 1: Check the Request URL**
Look at the URL that's causing the error. It will show which endpoint is being called:

```bash
# Reports API (expects YYYY-MM-DD)
/api/v1/reports/purchase/monthly?startDate=2024-01-01&endDate=2024-01-31

# Customer Debt API (expects YYYY-MM-DD)
/api/v1/customer-debts/date-range?startDate=2024-01-01&endDate=2024-01-31

# Money Box API (expects YYYY-MM-DDTHH:mm:ss)
/api/v1/moneybox/transactions?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

### **Step 2: Check Your Client Code**
Look for where you're sending `2024-01-01T00:00:00`:

```javascript
// ❌ Wrong - sending datetime to Reports API
const date = new Date('2024-01-01');
const wrongFormat = date.toISOString(); // "2024-01-01T00:00:00.000Z"

// ✅ Correct - sending date only to Reports API
const date = new Date('2024-01-01');
const correctFormat = date.toISOString().split('T')[0]; // "2024-01-01"
```

### **Step 3: Check API Documentation**
The Swagger/OpenAPI documentation should show the correct format for each endpoint.

## 📋 Endpoint Date Format Reference

### **Reports API Endpoints** (Use `YYYY-MM-DD`)
```bash
# All these endpoints expect YYYY-MM-DD format
/api/v1/reports/sales/daily?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/sales/monthly?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/profit/daily?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/profit/monthly?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/inventory/movement?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/purchase/daily?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/purchase/monthly?startDate=2024-01-01&endDate=2024-01-31

# Auto-pharmacy versions
/api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31
/api/v1/reports/my/sales/monthly?startDate=2024-01-01&endDate=2024-01-31
# ... etc
```

### **Customer Debt API** (Use `YYYY-MM-DD`)
```bash
/api/v1/customer-debts/date-range?startDate=2024-01-01&endDate=2024-01-31
```

### **Money Box API** (Use `YYYY-MM-DDTHH:mm:ss`)
```bash
/api/v1/moneybox/transactions?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

### **Purchase Invoice API** (Use `YYYY-MM-DDTHH:mm:ss`)
```bash
/api/v1/purchase-invoices/time-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

## 🔧 Quick Fixes

### **For Frontend JavaScript**
```javascript
// Function to format date correctly for Reports API
function formatDateForReports(date) {
    return date.toISOString().split('T')[0];
}

// Function to format date correctly for other APIs
function formatDateForOtherAPIs(date) {
    return date.toISOString();
}

// Usage
const date = new Date('2024-01-01');

// For Reports API
const reportsFormat = formatDateForReports(date); // "2024-01-01"

// For other APIs
const otherFormat = formatDateForOtherAPIs(date); // "2024-01-01T00:00:00.000Z"
```

### **For Backend Java**
```java
// For Reports API endpoints
LocalDate startDate = LocalDate.of(2024, 1, 1); // 2024-01-01

// For other APIs
LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0); // 2024-01-01T00:00:00
```

## 🧪 Testing Commands

### **Test Reports API (Should Work)**
```bash
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31"
```

### **Test Reports API (Should Fail)**
```bash
curl "http://localhost:8080/api/v1/reports/purchase/monthly?pharmacyId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-31"
```

### **Test Money Box API (Should Work)**
```bash
curl "http://localhost:8080/api/v1/moneybox/transactions?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"
```

## 🎯 Most Likely Causes

1. **Frontend sending wrong format**: Your client code is sending `2024-01-01T00:00:00` to Reports API
2. **Wrong endpoint**: You're calling a Reports API endpoint but using datetime format
3. **Cached requests**: Old requests in browser cache or API client cache
4. **Wrong API documentation**: Following wrong format in documentation

## 🔍 Debug Steps

1. **Check the exact URL** being called when the error occurs
2. **Verify the endpoint** matches the Reports API pattern
3. **Check your client code** for date formatting
4. **Clear browser cache** and API client cache
5. **Test with curl** to isolate the issue

## 📞 Next Steps

If you're still getting the error:

1. **Share the exact URL** that's causing the error
2. **Share your client code** that's making the request
3. **Check the browser network tab** for the exact request being sent
4. **Test with curl** to confirm the endpoint behavior

This will help identify exactly where the `2024-01-01T00:00:00` format is coming from.
