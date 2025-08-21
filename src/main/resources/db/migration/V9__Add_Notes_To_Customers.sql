-- Migration: Add notes field to customers table
-- Version: V9
-- Description: Add notes column to store customer notes and additional information

ALTER TABLE customers ADD COLUMN notes VARCHAR(500);

-- Add comment to the column
COMMENT ON COLUMN customers.notes IS 'Customer notes and additional information';

-- Update existing records to have empty notes (optional)
UPDATE customers SET notes = '' WHERE notes IS NULL;
