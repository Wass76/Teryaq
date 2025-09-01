-- Migration: V9__Fix_Schema_Issues
-- Description: Fix schema issues with existing data by properly handling column type changes and adding new columns with defaults

-- Fix money_box_transaction.created_by column type issue
-- First, check if the column exists and what type it is
DO $$
BEGIN
    -- Check if created_by column exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'money_box_transaction' 
        AND column_name = 'created_by'
    ) THEN
        -- Check the current data type
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'money_box_transaction' 
            AND column_name = 'created_by'
            AND data_type = 'integer'
        ) THEN
            -- Convert integer to bigint safely
            ALTER TABLE money_box_transaction 
            ALTER COLUMN created_by TYPE bigint USING created_by::bigint;
        ELSIF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'money_box_transaction' 
            AND column_name = 'created_by'
            AND data_type = 'character varying'
        ) THEN
            -- Convert varchar to bigint safely, setting NULL for non-numeric values
            ALTER TABLE money_box_transaction 
            ALTER COLUMN created_by TYPE bigint USING 
                CASE 
                    WHEN created_by ~ '^[0-9]+$' THEN created_by::bigint 
                    ELSE NULL 
                END;
        END IF;
    ELSE
        -- Column doesn't exist, add it
        ALTER TABLE money_box_transaction ADD COLUMN created_by bigint;
    END IF;
END $$;

-- Add new columns to sale_invoices table with proper defaults for existing data
-- Add status column with default value
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sale_invoices' 
        AND column_name = 'status'
    ) THEN
        ALTER TABLE sale_invoices ADD COLUMN status varchar(255) DEFAULT 'SOLD';
        -- Update existing records to have the default status
        UPDATE sale_invoices SET status = 'SOLD' WHERE status IS NULL;
        -- Now make it NOT NULL
        ALTER TABLE sale_invoices ALTER COLUMN status SET NOT NULL;
        -- Add check constraint
        ALTER TABLE sale_invoices ADD CONSTRAINT chk_sale_invoices_status 
            CHECK (status IN ('SOLD','CANCELLED','VOID'));
    END IF;
END $$;

-- Add payment_status column with default value
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sale_invoices' 
        AND column_name = 'payment_status'
    ) THEN
        ALTER TABLE sale_invoices ADD COLUMN payment_status varchar(255) DEFAULT 'FULLY_PAID';
        -- Update existing records based on payment status
        UPDATE sale_invoices 
        SET payment_status = CASE 
            WHEN remaining_amount = 0 OR remaining_amount IS NULL THEN 'FULLY_PAID'
            WHEN paid_amount > 0 THEN 'PARTIALLY_PAID'
            ELSE 'UNPAID'
        END 
        WHERE payment_status IS NULL;
        -- Now make it NOT NULL
        ALTER TABLE sale_invoices ALTER COLUMN payment_status SET NOT NULL;
        -- Add check constraint
        ALTER TABLE sale_invoices ADD CONSTRAINT chk_sale_invoices_payment_status 
            CHECK (payment_status IN ('FULLY_PAID','PARTIALLY_PAID','UNPAID'));
    END IF;
END $$;

-- Add refund_status column with default value
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sale_invoices' 
        AND column_name = 'refund_status'
    ) THEN
        ALTER TABLE sale_invoices ADD COLUMN refund_status varchar(255) DEFAULT 'NO_REFUND';
        -- Update existing records to have the default refund status
        UPDATE sale_invoices SET refund_status = 'NO_REFUND' WHERE refund_status IS NULL;
        -- Now make it NOT NULL
        ALTER TABLE sale_invoices ALTER COLUMN refund_status SET NOT NULL;
        -- Add check constraint
        ALTER TABLE sale_invoices ADD CONSTRAINT chk_sale_invoices_refund_status 
            CHECK (refund_status IN ('NO_REFUND','PARTIALLY_REFUNDED','FULLY_REFUNDED'));
    END IF;
END $$;

-- Add refunded_quantity column to sale_invoice_items table with default value
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'sale_invoice_items' 
        AND column_name = 'refunded_quantity'
    ) THEN
        ALTER TABLE sale_invoice_items ADD COLUMN refunded_quantity integer DEFAULT 0;
        -- Update existing records to have 0 refunded quantity
        UPDATE sale_invoice_items SET refunded_quantity = 0 WHERE refunded_quantity IS NULL;
        -- Now make it NOT NULL
        ALTER TABLE sale_invoice_items ALTER COLUMN refunded_quantity SET NOT NULL;
    END IF;
END $$;

-- Add comments to document the new fields
COMMENT ON COLUMN sale_invoices.status IS 'Invoice status: SOLD, CANCELLED, VOID';
COMMENT ON COLUMN sale_invoices.payment_status IS 'Payment status: FULLY_PAID, PARTIALLY_PAID, UNPAID';
COMMENT ON COLUMN sale_invoices.refund_status IS 'Refund status: NO_REFUND, PARTIALLY_REFUNDED, FULLY_REFUNDED';
COMMENT ON COLUMN sale_invoice_items.refunded_quantity IS 'Quantity refunded from this item';
COMMENT ON COLUMN money_box_transaction.created_by IS 'User ID who created this transaction';
