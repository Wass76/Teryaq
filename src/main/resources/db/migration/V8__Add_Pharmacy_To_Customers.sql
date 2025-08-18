-- Add pharmacy_id column to customers table (initially nullable)
ALTER TABLE customers ADD COLUMN pharmacy_id BIGINT;

-- Check if there are any existing pharmacies, if not create a default one
INSERT INTO pharmacies (id, name, address, phone, email, created_at, created_by, created_by_user_type, is_active, is_system, is_system_generated, last_modified_by, last_modified_by_user_type, updated_at)
SELECT 1, 'Default Pharmacy', 'Default Address', 'Default Phone', 'default@pharmacy.com', NOW(), 1, 'SYSTEM', true, true, true, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM pharmacies LIMIT 1);

-- Update existing customers to have a default pharmacy
UPDATE customers SET pharmacy_id = (SELECT id FROM pharmacies LIMIT 1) WHERE pharmacy_id IS NULL;

-- Now make the column NOT NULL after all records have been updated
ALTER TABLE customers ALTER COLUMN pharmacy_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE customers ADD CONSTRAINT fk_customers_pharmacy 
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id);

-- Add index for better performance
CREATE INDEX idx_customers_pharmacy_id ON customers(pharmacy_id); 