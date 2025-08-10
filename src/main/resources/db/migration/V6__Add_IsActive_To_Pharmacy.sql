-- Add isActive column to pharmacy table
ALTER TABLE pharmacy ADD COLUMN is_active BOOLEAN;

-- Update existing pharmacies to set isActive based on registration completion
-- A pharmacy is considered active if it has name, license_number, address, and phone_number
UPDATE pharmacy 
SET is_active = CASE 
    WHEN name IS NOT NULL 
         AND license_number IS NOT NULL 
         AND address IS NOT NULL 
         AND phone_number IS NOT NULL 
         AND name != '' 
         AND license_number != '' 
         AND address != '' 
         AND phone_number != ''
    THEN true 
    ELSE false 
END;

-- Set default value for new records
ALTER TABLE pharmacy ALTER COLUMN is_active SET DEFAULT false;
