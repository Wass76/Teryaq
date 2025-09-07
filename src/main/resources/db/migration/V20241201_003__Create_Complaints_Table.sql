-- Migration: Create complaints table
-- Description: Creates the complaints table for managing pharmacy complaints
-- Author: System
-- Date: 2024-12-01

CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT 'Title of the complaint',
    description TEXT NOT NULL COMMENT 'Detailed description of the complaint',
    pharmacy_id BIGINT NOT NULL COMMENT 'ID of the pharmacy',
    created_by BIGINT NOT NULL COMMENT 'ID of the user who created the complaint',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Status of the complaint: PENDING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED',
    response TEXT COMMENT 'Response from management',
    responded_by BIGINT COMMENT 'ID of the user who responded',
    responded_at DATETIME COMMENT 'Timestamp when the complaint was responded to',
    
    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the complaint was created',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when the complaint was last updated',
    updated_by BIGINT COMMENT 'ID of the user who last updated the complaint',
    
    -- Enhanced audit fields for comprehensive tracking
    ip_address VARCHAR(45) COMMENT 'User IP address',
    user_agent VARCHAR(500) COMMENT 'User browser/client info',
    session_id VARCHAR(100) COMMENT 'User session identifier',
    user_type VARCHAR(50) COMMENT 'Type of user (PHARMACIST, ADMIN, etc.)',
    additional_data TEXT COMMENT 'JSON string for additional context',
    
    -- Indexes
    INDEX idx_complaints_pharmacy_id (pharmacy_id),
    INDEX idx_complaints_created_by (created_by),
    INDEX idx_complaints_status (status),
    INDEX idx_complaints_created_at (created_at),
    INDEX idx_complaints_pharmacy_status (pharmacy_id, status),
    INDEX idx_complaints_responded_by (responded_by),
    
    -- Foreign key constraints (if referenced tables exist)
    -- FOREIGN KEY (pharmacy_id) REFERENCES pharmacy(id) ON DELETE CASCADE,
    -- FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE CASCADE,
    -- FOREIGN KEY (responded_by) REFERENCES user(id) ON DELETE SET NULL,
    -- FOREIGN KEY (updated_by) REFERENCES user(id) ON DELETE SET NULL
);

-- Add comments to the table
ALTER TABLE complaints COMMENT = 'Table for storing pharmacy complaints and their management';

-- Insert sample data for testing (optional)
INSERT INTO complaints (title, description, pharmacy_id, created_by, status, created_at) VALUES
('System Performance Issue', 'The system is running very slowly during peak hours, affecting customer service.', 1, 1, 'PENDING', NOW()),
('Inventory Management Problem', 'Some products are not showing correct stock levels in the system.', 1, 2, 'IN_PROGRESS', NOW()),
('Payment Gateway Issue', 'Credit card payments are failing intermittently.', 1, 1, 'RESOLVED', NOW());
