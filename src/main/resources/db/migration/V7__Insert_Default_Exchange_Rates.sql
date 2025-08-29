-- Migration: V7__Insert_Default_Exchange_Rates
-- Description: Insert default exchange rates with new 10000 SYP per USD rate
-- Date: 2024-12-19

-- Insert default exchange rates
-- Note: These rates will be automatically reversed by the ExchangeRate service

-- USD to SYP (1 USD = 10000 SYP)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('USD', 'SYP', 10000.00, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default exchange rate: 1 USD = 10000 SYP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EUR to SYP (1 EUR = 11000 SYP)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('EUR', 'SYP', 11000.00, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default exchange rate: 1 EUR = 11000 SYP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- SYP to USD (1 SYP = 0.0001 USD)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('SYP', 'USD', 0.0001, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default exchange rate: 1 SYP = 0.0001 USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- SYP to EUR (1 SYP = 0.000090909 EUR)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('SYP', 'EUR', 0.000090909, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default exchange rate: 1 SYP = 0.000090909 EUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- USD to EUR (1 USD = 0.909091 EUR)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('USD', 'EUR', 0.909091, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default cross-rate: 1 USD = 0.909091 EUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- EUR to USD (1 EUR = 1.1 USD)
INSERT INTO exchange_rate (from_currency, to_currency, rate, is_active, effective_from, source, notes, created_at, updated_at)
VALUES ('EUR', 'USD', 1.1, true, CURRENT_TIMESTAMP, 'MIGRATION_DEFAULT', 'Default cross-rate: 1 EUR = 1.1 USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add comments for documentation
COMMENT ON TABLE exchange_rate IS 'Exchange rates for currency conversion. All rates are automatically converted to SYP for MoneyBox operations.';
COMMENT ON COLUMN exchange_rate.source IS 'Source of the exchange rate (MIGRATION_DEFAULT, MANUAL, API, etc.)';
COMMENT ON COLUMN exchange_rate.notes IS 'Additional information about the exchange rate';
