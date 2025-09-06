-- Migration: Enhance MoneyBoxTransaction for Comprehensive Auditing
-- Description: Adds missing audit fields to existing MoneyBoxTransaction table
-- Author: System
-- Date: 2024-12-01

-- Add missing audit fields to money_box_transaction table
-- Note: entity_type and entity_id are already covered by reference_type and reference_id
ALTER TABLE money_box_transaction 
ADD COLUMN IF NOT EXISTS operation_status VARCHAR(20) DEFAULT 'SUCCESS',
ADD COLUMN IF NOT EXISTS error_message VARCHAR(2000),
ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45),
ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500),
ADD COLUMN IF NOT EXISTS session_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS user_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS additional_data TEXT,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_modified_by BIGINT,
ADD COLUMN IF NOT EXISTS last_modified_by_user_type VARCHAR(50);

-- Add indexes for new audit fields
-- Note: Using reference_type and reference_id instead of entity_type and entity_id
CREATE INDEX IF NOT EXISTS idx_moneybox_transaction_reference ON money_box_transaction(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_moneybox_transaction_status ON money_box_transaction(operation_status);
CREATE INDEX IF NOT EXISTS idx_moneybox_transaction_user_type ON money_box_transaction(user_type);
CREATE INDEX IF NOT EXISTS idx_moneybox_transaction_updated_at ON money_box_transaction(updated_at);

-- Add foreign key constraints for new fields
ALTER TABLE money_box_transaction 
ADD CONSTRAINT fk_moneybox_transaction_last_modified_by 
FOREIGN KEY (last_modified_by) REFERENCES users(id);

-- Update existing records to have default values
UPDATE money_box_transaction 
SET operation_status = 'SUCCESS',
    user_type = 'SYSTEM'
WHERE operation_status IS NULL;

-- Create enhanced view for financial analytics
CREATE OR REPLACE VIEW moneybox_financial_analytics AS
SELECT 
    mbt.money_box_id,
    mbt.transaction_type,
    mbt.original_currency,
    COUNT(*) as transaction_count,
    SUM(mbt.original_amount) as total_original_amount,
    SUM(mbt.converted_amount) as total_converted_amount,
    AVG(mbt.exchange_rate) as avg_exchange_rate,
    MIN(mbt.created_at) as first_transaction,
    MAX(mbt.created_at) as last_transaction,
    SUM(CASE WHEN mbt.operation_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count
FROM money_box_transaction mbt
GROUP BY mbt.money_box_id, mbt.transaction_type, mbt.original_currency;

-- Create view for currency conversion analysis
CREATE OR REPLACE VIEW moneybox_currency_analysis AS
SELECT 
    mbt.original_currency,
    mbt.converted_currency,
    COUNT(*) as conversion_count,
    SUM(mbt.original_amount) as total_original_amount,
    SUM(mbt.converted_amount) as total_converted_amount,
    AVG(mbt.exchange_rate) as avg_exchange_rate,
    MIN(mbt.exchange_rate) as min_exchange_rate,
    MAX(mbt.exchange_rate) as max_exchange_rate,
    MIN(mbt.conversion_timestamp) as first_conversion,
    MAX(mbt.conversion_timestamp) as last_conversion
FROM money_box_transaction mbt
WHERE mbt.original_currency != mbt.converted_currency
GROUP BY mbt.original_currency, mbt.converted_currency;

-- Create function to get money box financial summary
CREATE OR REPLACE FUNCTION get_moneybox_financial_summary(p_money_box_id BIGINT, p_start_date TIMESTAMP, p_end_date TIMESTAMP)
RETURNS TABLE(
    transaction_type VARCHAR(50),
    transaction_count BIGINT,
    total_amount DECIMAL(15,2),
    avg_amount DECIMAL(15,2),
    failed_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        mbt.transaction_type::VARCHAR(50),
        COUNT(*) as transaction_count,
        SUM(mbt.converted_amount) as total_amount,
        AVG(mbt.converted_amount) as avg_amount,
        SUM(CASE WHEN mbt.operation_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count
    FROM money_box_transaction mbt
    WHERE mbt.money_box_id = p_money_box_id
      AND mbt.created_at BETWEEN p_start_date AND p_end_date
    GROUP BY mbt.transaction_type;
END;
$$ LANGUAGE plpgsql;

-- Grant permissions
GRANT SELECT ON moneybox_financial_analytics TO app_user;
GRANT SELECT ON moneybox_currency_analysis TO app_user;
GRANT EXECUTE ON FUNCTION get_moneybox_financial_summary(BIGINT, TIMESTAMP, TIMESTAMP) TO app_user;

-- Add comments
COMMENT ON VIEW moneybox_financial_analytics IS 'Enhanced financial analytics view for MoneyBox transactions';
COMMENT ON VIEW moneybox_currency_analysis IS 'Currency conversion analysis for MoneyBox transactions';
COMMENT ON FUNCTION get_moneybox_financial_summary(BIGINT, TIMESTAMP, TIMESTAMP) IS 'Get financial summary for MoneyBox transactions in date range';
