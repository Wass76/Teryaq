-- Add missing fields to exchange_rate table
ALTER TABLE exchange_rate 
ADD COLUMN IF NOT EXISTS effective_from TIMESTAMP,
ADD COLUMN IF NOT EXISTS effective_to TIMESTAMP,
ADD COLUMN IF NOT EXISTS source VARCHAR(100),
ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

-- Update existing records to have default values
UPDATE exchange_rate 
SET effective_from = created_at,
    effective_to = NULL,
    source = 'SYSTEM',
    notes = 'Migrated from existing data'
WHERE effective_from IS NULL;

-- Make sure the columns are not null for new records
ALTER TABLE exchange_rate 
ALTER COLUMN effective_from SET NOT NULL,
ALTER COLUMN source SET NOT NULL;

-- Note: from_currency and to_currency columns should already exist and contain 
-- valid currency codes (SYP, USD, EUR) that match the Currency enum values
