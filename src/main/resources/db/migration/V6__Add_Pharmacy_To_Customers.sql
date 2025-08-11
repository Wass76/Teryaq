-- Add pharmacy_id column to customers table (initially nullable)
ALTER TABLE customers ADD COLUMN pharmacy_id BIGINT;

-- Update existing customers to have a default pharmacy (you may need to adjust this based on your data)
-- This assumes you have at least one pharmacy in the system
UPDATE customers SET pharmacy_id = (SELECT id FROM pharmacy LIMIT 1) WHERE pharmacy_id IS NULL;

-- Now make the column NOT NULL after all records have been updated
ALTER TABLE customers ALTER COLUMN pharmacy_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE customers ADD CONSTRAINT fk_customers_pharmacy 
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacy(id);

-- Add index for better performance
CREATE INDEX idx_customers_pharmacy_id ON customers(pharmacy_id); 