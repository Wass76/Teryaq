-- Add minStockLevel field to pharmacy_product table
ALTER TABLE pharmacy_product 
ADD COLUMN min_stock_level INTEGER;

-- Add comment for documentation
COMMENT ON COLUMN pharmacy_product.min_stock_level IS 'Minimum stock level threshold for re-purchase notification';
