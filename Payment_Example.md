# مثال على نظام الدفع الجديد

## 🔄 **أنواع الدفع المدعومة:**

### 1. **CASH (نقدي)**
- **CASH_BOX**: صندوق الكاش
- **BANK_ACCOUNT**: حساب البنك

### 2. **CREDIT (دين)**
- **BANK_ACCOUNT**: حساب البنك

## 📋 **مثال على API Call للبيع:**

```bash
curl -X 'POST' \
  'http://159.198.75.161:13000/api/sales' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH_BOX",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 10,
  "paidAmount": 1000,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 5,
      "unitPrice": 100,
      "discountType": "FIXED_AMOUNT",
      "discountValue": 50
    },
    {
      "stockItemId": 2,
      "quantity": 3,
      "unitPrice": 200,
      "discountType": "PERCENTAGE",
      "discountValue": 15
    }
  ]
}'
```

## 🔍 **APIs للدفع:**

### 1. **الحصول على أنواع الدفع:**
```bash
GET /api/payment/types
```

**Response:**
```json
[
  {
    "code": "CASH",
    "arabicName": "نقدي",
    "englishName": "Cash"
  },
  {
    "code": "CREDIT",
    "arabicName": "دين",
    "englishName": "Credit"
  }
]
```

### 2. **الحصول على وسائل الدفع:**
```bash
GET /api/payment/methods
```

**Response:**
```json
[
  {
    "code": "CASH_BOX",
    "arabicName": "صندوق الكاش",
    "englishName": "Cash Box"
  },
  {
    "code": "BANK_ACCOUNT",
    "arabicName": "حساب البنك",
    "englishName": "Bank Account"
  }
]
```

### 3. **الحصول على وسائل الدفع المتوافقة:**
```bash
GET /api/payment/methods/CASH
```

**Response:**
```json
[
  {
    "code": "CASH_BOX",
    "arabicName": "صندوق الكاش",
    "englishName": "Cash Box"
  },
  {
    "code": "BANK_ACCOUNT",
    "arabicName": "حساب البنك",
    "englishName": "Bank Account"
  }
]
```

### 4. **التحقق من صحة الدفع:**
```bash
POST /api/payment/validate
Content-Type: application/json

{
  "paymentType": "CASH",
  "paymentMethod": "CASH_BOX"
}
```

**Response:**
```json
{
  "valid": true,
  "message": "الدفع صحيح"
}
```

## ✅ **قواعد التحقق:**

### **للدفع النقدي (CASH):**
- ✅ يمكن الدفع كاملاً أو جزئياً
- ✅ وسائل الدفع: كاش أو حساب بنك
- ✅ المبلغ المدفوع يجب أن يكون ≥ 0 و ≤ إجمالي الفاتورة

### **للدفع الآجل (CREDIT):**
- ✅ يمكن الدفع جزئياً
- ✅ وسيلة الدفع: حساب بنك فقط
- ✅ المبلغ المدفوع يجب أن يكون ≥ 0

## 🎯 **مزايا النظام الجديد:**

1. **مرونة**: دعم نوعين من الدفع
2. **أمان**: التحقق من صحة الدفع
3. **دقة**: حساب دقيق للمبالغ المتبقية
4. **توافق**: دعم الكود القديم
5. **وضوح**: رسائل خطأ واضحة بالعربية 