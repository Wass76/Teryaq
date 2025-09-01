-- Migration: Add Payment and Refund Status to Sale Invoices
-- This migration adds the new status columns for better invoice tracking

-- Add payment_status column
ALTER TABLE sale_invoices 
ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'FULLY_PAID';

-- Add refund_status column  
ALTER TABLE sale_invoices 
ADD COLUMN refund_status VARCHAR(20) NOT NULL DEFAULT 'NO_REFUND';

-- Update existing records to have appropriate default values
-- All existing invoices are considered fully paid and have no refunds
UPDATE sale_invoices 
SET payment_status = CASE 
    WHEN remaining_amount = 0 THEN 'FULLY_PAID'
    WHEN paid_amount > 0 THEN 'PARTIALLY_PAID'
    ELSE 'UNPAID'
END,
refund_status = 'NO_REFUND';

-- Add comments for documentation
COMMENT ON COLUMN sale_invoices.payment_status IS 'Payment status: FULLY_PAID, PARTIALLY_PAID, UNPAID';
COMMENT ON COLUMN sale_invoices.refund_status IS 'Refund status: NO_REFUND, PARTIALLY_REFUNDED, FULLY_REFUNDED';
