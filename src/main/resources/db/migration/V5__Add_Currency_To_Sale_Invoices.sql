-- Add currency column to sale_invoices table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sale_invoices' 
        AND column_name = 'currency'
    ) THEN
        ALTER TABLE sale_invoices ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'SYP';
    END IF;
END $$;

-- Update existing records to have SYP as default currency
UPDATE sale_invoices SET currency = 'SYP' WHERE currency IS NULL; 