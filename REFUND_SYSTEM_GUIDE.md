# دليل نظام المرتجعات الشامل - دعم فواتير الدين

## 📋 نظرة عامة

تم تطوير نظام مرتجعات شامل يدعم جميع أنواع الفواتير بما في ذلك فواتير الدين، مع معالجة ذكية للصندوق والديون.

## 🎯 الحالات المدعومة

### 1. فاتورة نقدية مدفوعة بالكامل
- **المرتجع**: إرجاع نقدي كامل للصندوق
- **المخزون**: إرجاع تلقائي للمخزون
- **الحالة**: `FULL_CASH_REFUND`

### 2. فاتورة دين مدفوعة جزئياً
- **المرتجع**: 
  - إرجاع نقدي للجزء المدفوع
  - خصم من دين العميل للجزء المتبقي
- **المخزون**: إرجاع تلقائي للمخزون
- **الحالة**: `PARTIAL_CASH_AND_DEBT_REDUCTION`

### 3. فاتورة دين غير مدفوعة
- **المرتجع**: خصم من دين العميل فقط
- **المخزون**: إرجاع تلقائي للمخزون
- **الحالة**: `DEBT_REDUCTION_ONLY`

### 4. فاتورة نقدية مدفوعة جزئياً (نادرة)
- **المرتجع**: إرجاع نقدي للجزء المدفوع
- **المخزون**: إرجاع تلقائي للمخزون
- **الحالة**: `PARTIAL_CASH_REFUND`

## 🔧 التغييرات التقنية

### 1. إزالة القيود
```java
// تم إزالة هذا القيد
// if (saleInvoice.getRemainingAmount() > 0) {
//     throw new RequestNotValidException("Cannot refund a partially paid sale invoice");
// }
```

### 2. معالجة ذكية للمرتجع
```java
private void handleRefundPayment(SaleInvoice saleInvoice, float totalRefundAmount, Long currentPharmacyId, Long saleId) {
    // معالجة حسب نوع الدفع وحالة الفاتورة
}
```

### 3. خصم من دين العميل
```java
private void reduceCustomerDebt(Customer customer, float amount, Long saleId) {
    // البحث عن دين مرتبط بالفاتورة أو أحدث دين نشط
    // خصم المبلغ وتحديث الحالة
}
```

## 📊 API Endpoints

### 1. إنشاء مرتجع
```http
POST /api/v1/sales/{saleId}/refund
```

**Request Body:**
```json
{
  "refundItems": [
    {
      "itemId": 15,
      "quantity": 2,
      "itemRefundReason": "العميل طلب إرجاع"
    }
  ],
  "refundReason": "إرجاع طلب العميل"
}
```

### 2. تفاصيل المرتجع مع معلومات الدين
```http
GET /api/v1/sales/refunds/{refundId}/details
```

**Response:**
```json
{
  "refundId": 1,
  "saleInvoiceId": 100,
  "customerId": 5,
  "customerName": "أحمد محمد",
  "totalRefundAmount": 150.0,
  "refundType": "PARTIAL_CASH_AND_DEBT_REDUCTION",
  "cashRefundAmount": 50.0,
  "debtReductionAmount": 100.0,
  "originalInvoicePaidAmount": 200.0,
  "originalInvoiceRemainingAmount": 300.0,
  "paymentType": "CREDIT",
  "paymentMethod": "BANK_ACCOUNT",
  "currency": "SYP",
  "refundReason": "إرجاع طلب العميل",
  "refundDate": "2024-01-15T10:30:00",
  "stockRestored": true,
  "customerTotalDebt": 500.0,
  "activeDebtsCount": 2,
  "refundedItems": [
    {
      "productName": "باراسيتامول 500ملغ",
      "quantity": 2,
      "unitPrice": 75.0,
      "subtotal": 150.0,
      "itemRefundReason": "العميل طلب إرجاع",
      "stockRestored": true
    }
  ]
}
```

## 💰 معالجة المدفوعات

### خوارزمية حساب المرتجع

```java
// الحالة 1: فاتورة نقدية مدفوعة بالكامل
if (paymentType == CASH && remainingAmount == 0) {
    cashRefund = totalRefundAmount;
    debtReduction = 0;
}

// الحالة 2: فاتورة دين مدفوعة جزئياً
else if (paymentType == CREDIT && paidAmount > 0) {
    cashRefund = Math.min(totalRefundAmount, paidAmount);
    debtReduction = totalRefundAmount - cashRefund;
}

// الحالة 3: فاتورة دين غير مدفوعة
else if (paymentType == CREDIT && paidAmount == 0) {
    cashRefund = 0;
    debtReduction = totalRefundAmount;
}
```

## 🏦 معالجة الصندوق

### تسجيل في MoneyBox
- **النقد**: تسجيل كـ `CASH_WITHDRAWAL` (سحب)
- **العملة**: تحويل تلقائي إلى SYP إذا لزم الأمر
- **التفاصيل**: تسجيل تفاصيل المرتجع والفاتورة

### مثال على التسجيل
```java
salesIntegrationService.recordSaleRefund(
    currentPharmacyId,
    saleId,
    BigDecimal.valueOf(cashRefundAmount),
    saleInvoice.getCurrency()
);
```

## 📈 تتبع الديون

### خصم من دين العميل
1. **البحث عن الدين**: البحث عن دين مرتبط بالفاتورة أو أحدث دين نشط
2. **الخصم**: خصم المبلغ من `remainingAmount`
3. **التحديث**: تحديث `paidAmount` و `status`
4. **التسجيل**: تسجيل `paidAt` إذا تم السداد بالكامل

### مثال على الخصم
```java
CustomerDebt debt = debts.get(0);
float currentRemaining = debt.getRemainingAmount();
float newRemaining = Math.max(0, currentRemaining - amount);

debt.setRemainingAmount(newRemaining);
debt.setPaidAmount(debt.getPaidAmount() + (currentRemaining - newRemaining));

if (newRemaining == 0) {
    debt.setStatus("PAID");
    debt.setPaidAt(LocalDateTime.now());
}
```

## 🔍 مراقبة وتتبع

### Logging شامل
```java
logger.info("Cash refund recorded in Money Box for invoice: {}", saleId);
logger.info("Customer debt reduced by {} for refund invoice: {}", debtReduction, saleId);
logger.info("Reduced customer debt: customer={}, debt={}, amount={}, newRemaining={}", 
           customer.getId(), debt.getId(), amount, newRemaining);
```

### إحصائيات المرتجع
- نوع المرتجع
- المبلغ النقدي المرتجع
- المبلغ المخصوم من الدين
- حالة المخزون
- معلومات العميل والدين

## ⚠️ اعتبارات مهمة

### 1. الأمان
- التحقق من الصيدلية لكل عملية
- التحقق من صلاحيات المستخدم
- تسجيل جميع العمليات

### 2. التكامل
- معالجة الأخطاء بشكل آمن
- Rollback في حالة الفشل
- عدم فقدان البيانات

### 3. الأداء
- استخدام Transactions
- تحسين الاستعلامات
- Caching عند الحاجة

## 🧪 اختبار الحالات

### حالة اختبار 1: فاتورة دين غير مدفوعة
```json
{
  "customerId": 1,
  "paymentType": "CREDIT",
  "paidAmount": 0.0,
  "totalAmount": 500.0,
  "remainingAmount": 500.0
}
```

### حالة اختبار 2: فاتورة دين مدفوعة جزئياً
```json
{
  "customerId": 1,
  "paymentType": "CREDIT",
  "paidAmount": 200.0,
  "totalAmount": 500.0,
  "remainingAmount": 300.0
}
```

### حالة اختبار 3: فاتورة نقدية
```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paidAmount": 500.0,
  "totalAmount": 500.0,
  "remainingAmount": 0.0
}
```

## 📝 ملاحظات التنفيذ

1. **التوافق مع الأنظمة الحالية**: الحل متوافق مع النظام الحالي
2. **قابلية التوسع**: يمكن إضافة حالات جديدة بسهولة
3. **المرونة**: يدعم جميع أنواع الفواتير والمدفوعات
4. **التتبع**: تسجيل شامل لجميع العمليات
5. **الأمان**: حماية من الأخطاء والاحتيال

## 🎉 المزايا

✅ **دعم فواتير الدين**: يمكن إرجاع فواتير الدين الآن
✅ **معالجة ذكية**: معالجة تلقائية حسب نوع الفاتورة
✅ **تتبع شامل**: معلومات مفصلة عن كل مرتجع
✅ **تكامل مع الصندوق**: تسجيل تلقائي في MoneyBox
✅ **إدارة الديون**: خصم تلقائي من دين العميل
✅ **إرجاع المخزون**: إرجاع تلقائي للمخزون
✅ **API شامل**: endpoints لجميع العمليات
✅ **توثيق مفصل**: دليل شامل للاستخدام
