-- Migration: V6__Enhance_MoneyBox_Transaction_Currency_Support
-- Description: Add currency conversion support to money_box_transaction table
-- Date: 2024-12-19

-- Add new currency conversion columns to money_box_transaction table
ALTER TABLE money_box_transaction 
ADD COLUMN original_currency VARCHAR(3),
ADD COLUMN original_amount DECIMAL(15,2),
ADD COLUMN converted_currency VARCHAR(3),
ADD COLUMN converted_amount DECIMAL(15,2),
ADD COLUMN exchange_rate DECIMAL(15,6),
ADD COLUMN conversion_timestamp TIMESTAMP,
ADD COLUMN conversion_source VARCHAR(100);

-- Update existing records to set default values
UPDATE money_box_transaction 
SET 
    original_currency = CASE 
        WHEN currency IS NOT NULL THEN currency 
        ELSE 'SYP' 
    END,
    original_amount = amount,
    converted_currency = 'SYP',
    converted_amount = amount,
    exchange_rate = 1.0,
    conversion_timestamp = created_at,
    conversion_source = 'MIGRATION_DEFAULT'
WHERE original_currency IS NULL;

-- Make new columns NOT NULL after setting default values
ALTER TABLE money_box_transaction 
ALTER COLUMN original_currency SET NOT NULL,
ALTER COLUMN original_amount SET NOT NULL,
ALTER COLUMN converted_currency SET NOT NULL,
ALTER COLUMN converted_amount SET NOT NULL,
ALTER COLUMN exchange_rate SET NOT NULL,
ALTER COLUMN conversion_timestamp SET NOT NULL,
ALTER COLUMN conversion_source SET NOT NULL;

-- Add indexes for better performance on currency-related queries
CREATE INDEX idx_moneybox_transaction_original_currency ON money_box_transaction(original_currency);
CREATE INDEX idx_moneybox_transaction_converted_currency ON money_box_transaction(converted_currency);
CREATE INDEX idx_moneybox_transaction_conversion_timestamp ON money_box_transaction(conversion_timestamp);

-- Add comments for documentation
COMMENT ON COLUMN money_box_transaction.original_currency IS 'Original currency of the transaction';
COMMENT ON COLUMN money_box_transaction.original_amount IS 'Original amount in original currency';
COMMENT ON COLUMN money_box_transaction.converted_currency IS 'Currency after conversion (always SYP)';
COMMENT ON COLUMN money_box_transaction.converted_amount IS 'Amount after conversion to SYP';
COMMENT ON COLUMN money_box_transaction.exchange_rate IS 'Exchange rate used for conversion';
COMMENT ON COLUMN money_box_transaction.conversion_timestamp IS 'When the currency conversion was performed';
COMMENT ON COLUMN money_box_transaction.conversion_source IS 'Source of the exchange rate (e.g., EXCHANGE_RATE_SERVICE, FALLBACK)';
