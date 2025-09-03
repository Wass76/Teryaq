# 📊 Reports Endpoint Comparison

## Overview
This document compares the two versions of Reports API endpoints:
1. **Original Endpoints** (with `pharmacyId` parameter)
2. **Auto-Pharmacy Endpoints** (no `pharmacyId` parameter, auto-extracts from current user)

---

## 🔄 Endpoint Comparison Table

| Report Category | Original Endpoint | Auto-Pharmacy Endpoint | Key Difference |
|-----------------|-------------------|------------------------|----------------|
| **Sales Daily** | `/api/v1/reports/sales/daily` | `/api/v1/reports/my/sales/daily` | No `pharmacyId` required |
| **Sales Monthly** | `/api/v1/reports/sales/monthly` | `/api/v1/reports/my/sales/monthly` | No `pharmacyId` required |
| **Profit Daily** | `/api/v1/reports/profit/daily` | `/api/v1/reports/my/profit/daily` | No `pharmacyId` required |
| **Profit Monthly** | `/api/v1/reports/profit/monthly` | `/api/v1/reports/my/profit/monthly` | No `pharmacyId` required |
| **Inventory Current** | `/api/v1/reports/inventory/current` | `/api/v1/reports/my/inventory/current` | No `pharmacyId` required |
| **Inventory Movement** | `/api/v1/reports/inventory/movement` | `/api/v1/reports/my/inventory/movement` | No `pharmacyId` required |
| **Debt Summary** | `/api/v1/reports/debt/summary` | `/api/v1/reports/my/debt/summary` | No `pharmacyId` required |
| **Purchase Daily** | `/api/v1/reports/purchase/daily` | `/api/v1/reports/my/purchase/daily` | No `pharmacyId` required |
| **Purchase Monthly** | `/api/v1/reports/purchase/monthly` | `/api/v1/reports/my/purchase/monthly` | No `pharmacyId` required |

---

## 📋 Request Parameters Comparison

### **Original Endpoints** (with pharmacyId)
```bash
# Example: Daily Sales Report
GET /api/v1/reports/sales/daily?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN
```

**Required Parameters:**
- `pharmacyId` ✅ (Required)
- `startDate` ✅ (Required for date-range reports)
- `endDate` ✅ (Required for date-range reports)

**Optional Parameters:**
- `currency` ❌ (Default: SYP)
- `language` ❌ (Default: EN)

### **Auto-Pharmacy Endpoints** (no pharmacyId)
```bash
# Example: Daily Sales Report (My Pharmacy)
GET /api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN
```

**Required Parameters:**
- `startDate` ✅ (Required for date-range reports)
- `endDate` ✅ (Required for date-range reports)

**Optional Parameters:**
- `currency` ❌ (Default: SYP)
- `language` ❌ (Default: EN)

**Auto-Extracted:**
- `pharmacyId` 🔄 (Automatically extracted from current user)

---

## 🔐 Security & Access Control

### **Original Endpoints**
- **Target Users**: Admin users, system administrators
- **Access Level**: Cross-pharmacy access
- **Use Case**: Multi-tenant management, system-wide reporting
- **Security**: Requires explicit pharmacyId parameter
- **Validation**: Manual pharmacy ID validation

### **Auto-Pharmacy Endpoints**
- **Target Users**: Regular pharmacy users, employees
- **Access Level**: Single pharmacy access (current user's pharmacy)
- **Use Case**: Regular pharmacy operations, user-specific reporting
- **Security**: Inherits from `BaseSecurityService`
- **Validation**: Automatic pharmacy ID extraction and validation

---

## 🚀 Usage Examples

### **Original Endpoints** (Admin/Multi-tenant)

#### **Daily Sales Report (Any Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/sales/daily?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN"
```

#### **Current Inventory Report (Any Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/inventory/current?pharmacyId=2&currency=SYP&language=AR"
```

#### **Customer Debt Report (Any Pharmacy)**
```bash
curl -X GET "http://localhost:8080/api/v1/reports/debt/summary?pharmacyId=3&currency=SYP&language=EN"
```

### **Auto-Pharmacy Endpoints** (Current User's Pharmacy)

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

---

## 🔧 Technical Implementation

### **Service Layer Changes**
```java
// ReportService now extends BaseSecurityService
public class ReportService extends BaseSecurityService {
    
    // Auto-extract pharmacy ID from current user
    public ReportResponse generateReportWithCurrentUser(ReportRequest request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        request.setPharmacyId(currentPharmacyId.toString());
        return generateReport(request);
    }
}
```

### **Controller Layer Changes**
```java
// Original endpoint (with pharmacyId)
@GetMapping("/sales/daily")
public ResponseEntity<ReportResponse> getDailySalesReport(
    @RequestParam String pharmacyId,  // Required
    @RequestParam String startDate,
    @RequestParam String endDate) {
    // Uses pharmacyId from request
}

// Auto-pharmacy endpoint (no pharmacyId)
@GetMapping("/my/sales/daily")
public ResponseEntity<ReportResponse> getMyDailySalesReport(
    @RequestParam String startDate,    // No pharmacyId required
    @RequestParam String endDate) {
    // Auto-extracts pharmacyId from current user
}
```

---

## 📊 Response Structure

**Both endpoint versions return the same response structure:**

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
    "details": [...]
  },
  "metadata": {
    "generatedAt": "2024-01-15T10:30:00",
    "reportType": "DAILY_SALES_SUMMARY",
    "pharmacyId": "1",  // Auto-populated for /my/ endpoints
    "language": "EN"
  }
}
```

---

## 🎯 Use Case Scenarios

### **Scenario 1: Admin User**
- **User Type**: System Administrator
- **Need**: Generate reports for multiple pharmacies
- **Use**: Original endpoints with explicit `pharmacyId`
- **Example**: `/api/v1/reports/sales/daily?pharmacyId=1&startDate=2024-01-01&endDate=2024-01-31`

### **Scenario 2: Pharmacy Manager**
- **User Type**: Pharmacy Manager/Employee
- **Need**: Generate reports for their own pharmacy
- **Use**: Auto-pharmacy endpoints
- **Example**: `/api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31`

### **Scenario 3: Frontend Application**
- **User Type**: Web/Mobile App
- **Need**: Dynamic pharmacy selection
- **Use**: Original endpoints with dynamic `pharmacyId`
- **Example**: `/api/v1/reports/sales/daily?pharmacyId=${selectedPharmacy}&startDate=2024-01-01&endDate=2024-01-31`

### **Scenario 4: User Dashboard**
- **User Type**: Logged-in User
- **Need**: Personal pharmacy reports
- **Use**: Auto-pharmacy endpoints
- **Example**: `/api/v1/reports/my/sales/daily?startDate=2024-01-01&endDate=2024-01-31`

---

## 🔄 Migration Guide

### **For Existing Applications**
1. **Keep using original endpoints** if you need cross-pharmacy access
2. **Migrate to auto-pharmacy endpoints** if you only need current user's pharmacy
3. **Update frontend** to use `/my/` endpoints for user-specific features

### **For New Applications**
1. **Use auto-pharmacy endpoints** for regular user features
2. **Use original endpoints** for admin/management features
3. **Implement proper authentication** for auto-pharmacy endpoints

---

## ✅ Benefits of Auto-Pharmacy Endpoints

### **For Developers**
- **Simpler API calls** (no need to manage pharmacyId)
- **Reduced parameter complexity** (fewer required parameters)
- **Automatic security** (pharmacy access validation)

### **For Users**
- **Better user experience** (no need to select pharmacy)
- **Reduced errors** (no wrong pharmacyId selection)
- **Consistent access** (always current user's pharmacy)

### **For Security**
- **Automatic validation** (pharmacy access checked)
- **Reduced attack surface** (no pharmacyId injection)
- **Consistent permissions** (inherits from BaseSecurityService)

---

## ⚠️ Important Notes

1. **Authentication Required**: Auto-pharmacy endpoints require user authentication
2. **Pharmacy Association**: User must be associated with a pharmacy
3. **Backward Compatibility**: Original endpoints remain unchanged
4. **Error Handling**: Both versions handle errors consistently
5. **Performance**: No performance difference between versions

This dual-endpoint approach provides maximum flexibility while maintaining security and ease of use for different user types.
