# مثال على Response المرتجع مع معلومات العميل

## 📋 Response الجديد

```json
{
  "refundId": 1,
  "saleInvoiceId": 100,
  "totalRefundAmount": 150.0,
  "refundReason": "إرجاع طلب العميل",
  "refundDate": "2024-01-15T10:30:00",
  "stockRestored": true,
  
  // معلومات العميل
  "customerId": 5,
  "customerName": "أحمد محمد",
  "customerPhoneNumber": "0935123456",
  "customerAddress": "دمشق - المزة",
  "customerNotes": "عميل منتظم",
  
  // معلومات الفاتورة الأصلية
  "originalInvoiceAmount": 500.0,
  "originalInvoicePaidAmount": 200.0,
  "originalInvoiceRemainingAmount": 300.0,
  "paymentType": "CREDIT",
  "paymentMethod": "BANK_ACCOUNT",
  "currency": "SYP",
  
  // معلومات الدين الحالي للعميل
  "customerTotalDebt": 500.0,
  "customerActiveDebtsCount": 2,
  
  // المنتجات المرتجعة
  "refundedItems": [
    {
      "productName": "باراسيتامول 500ملغ",
      "quantity": 2,
      "unitPrice": 75.0,
      "subtotal": 150.0,
      "itemRefundReason": "العميل طلب إرجاع"
    }
  ]
}
```

## 🎯 المعلومات المضافة

### معلومات العميل:
- **customerId**: رقم العميل
- **customerName**: اسم العميل
- **customerPhoneNumber**: رقم الهاتف
- **customerAddress**: العنوان
- **customerNotes**: ملاحظات العميل

### معلومات الفاتورة الأصلية:
- **originalInvoiceAmount**: إجمالي الفاتورة الأصلية
- **originalInvoicePaidAmount**: المبلغ المدفوع
- **originalInvoiceRemainingAmount**: المبلغ المتبقي
- **paymentType**: نوع الدفع (CASH/CREDIT)
- **paymentMethod**: طريقة الدفع
- **currency**: العملة

### معلومات الدين الحالي:
- **customerTotalDebt**: إجمالي دين العميل الحالي
- **customerActiveDebtsCount**: عدد الديون النشطة للعميل

## 📊 الاستخدام

### 1. إنشاء مرتجع:
```http
POST /api/v1/sales/{saleId}/refund
```

### 2. جلب المرتجعات:
```http
GET /api/v1/sales/refunds
GET /api/v1/sales/{saleId}/refunds
GET /api/v1/sales/refunds/date-range?startDate=2024-01-01&endDate=2024-01-31
```

### 3. تفاصيل المرتجع:
```http
GET /api/v1/sales/refunds/{refundId}/details
```

## 💡 المزايا

✅ **معلومات شاملة**: جميع معلومات العميل والفاتورة والدين  
✅ **تتبع الدين**: معرفة الدين الحالي للعميل بعد المرتجع  
✅ **سهولة الاستخدام**: معلومات منظمة وواضحة  
✅ **توافق مع النظام**: يعمل مع جميع endpoints الموجودة  
✅ **أداء محسن**: حساب الدين مرة واحدة لكل response  
