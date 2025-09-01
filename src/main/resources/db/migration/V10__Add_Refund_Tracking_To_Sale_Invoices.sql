-- إضافة حقل تتبع الكمية المرتجعة في sale_invoice_items
ALTER TABLE sale_invoice_items 
ADD COLUMN refunded_quantity INTEGER DEFAULT 0;

-- إضافة حقل حالة الفاتورة في sale_invoices (بدون NOT NULL أولاً)
ALTER TABLE sale_invoices 
ADD COLUMN status VARCHAR(20) DEFAULT 'SOLD';

-- تحديث البيانات الموجودة أولاً
UPDATE sale_invoices SET status = 'SOLD' WHERE status IS NULL;
UPDATE sale_invoice_items SET refunded_quantity = 0 WHERE refunded_quantity IS NULL;

-- الآن نجعل الحقول NOT NULL بعد تحديث البيانات
ALTER TABLE sale_invoice_items 
ALTER COLUMN refunded_quantity SET NOT NULL;

ALTER TABLE sale_invoices 
ALTER COLUMN status SET NOT NULL;

-- إنشاء index لتحسين الأداء
CREATE INDEX idx_sale_invoices_status ON sale_invoices(status);
CREATE INDEX idx_sale_invoice_items_refunded_quantity ON sale_invoice_items(refunded_quantity);
