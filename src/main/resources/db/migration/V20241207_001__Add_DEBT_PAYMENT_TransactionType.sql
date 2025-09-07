-- Migration: Add DEBT_PAYMENT Transaction Type
-- Description: Updates the money_box_transaction_transaction_type_check constraint to include DEBT_PAYMENT
-- Author: System
-- Date: 2024-12-07

-- Drop the existing constraint
ALTER TABLE money_box_transaction DROP CONSTRAINT IF EXISTS money_box_transaction_transaction_type_check;

-- Add the updated constraint with DEBT_PAYMENT included
ALTER TABLE money_box_transaction ADD CONSTRAINT money_box_transaction_transaction_type_check 
    CHECK (transaction_type IN (
        'OPENING_BALANCE',
        'CASH_DEPOSIT', 
        'CASH_WITHDRAWAL',
        'SALE_PAYMENT',
        'SALE_REFUND',
        'PURCHASE_PAYMENT',
        'PURCHASE_REFUND',
        'DEBT_PAYMENT',  -- ✅ ADDED: For customer debt payments
        'EXPENSE',
        'INCOME',
        'TRANSFER_IN',
        'TRANSFER_OUT',
        'ADJUSTMENT',
        'CLOSING_BALANCE'
    ));

-- Add comment
COMMENT ON CONSTRAINT money_box_transaction_transaction_type_check ON money_box_transaction 
IS 'Check constraint for valid transaction types including DEBT_PAYMENT';
