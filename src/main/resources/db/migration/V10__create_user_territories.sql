-- Create user_territories table
CREATE TABLE IF NOT EXISTS user_territories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    level VARCHAR(20) NOT NULL CHECK (level IN ('CITY', 'PROVINCE', 'COUNTRY')),
    city_id BIGINT REFERENCES system_city(id),
    province_id BIGINT REFERENCES system_province(id),
    country_id BIGINT REFERENCES system_country(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    
    -- Ensure only one geographical entity is set based on level
    CONSTRAINT check_territory_level CHECK (
        (level = 'CITY' AND city_id IS NOT NULL AND province_id IS NULL AND country_id IS NULL) OR
        (level = 'PROVINCE' AND city_id IS NULL AND province_id IS NOT NULL AND country_id IS NULL) OR
        (level = 'COUNTRY' AND city_id IS NULL AND province_id IS NULL AND country_id IS NOT NULL)
    )
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_territories_user_active ON user_territories (user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_user_territories_level ON user_territories (level);
CREATE INDEX IF NOT EXISTS idx_user_territories_city ON user_territories (city_id) WHERE city_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_territories_province ON user_territories (province_id) WHERE province_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_territories_country ON user_territories (country_id) WHERE country_id IS NOT NULL;

-- Create composite indexes for geographical queries
CREATE INDEX IF NOT EXISTS idx_leads_geographical ON leads (city_id, province_id, country_id);
CREATE INDEX IF NOT EXISTS idx_dealer_geographical ON dealer (city_id, province_id, country_id);

-- Add comments for documentation
COMMENT ON TABLE user_territories IS 'Stores geographical territory assignments for users';
COMMENT ON COLUMN user_territories.level IS 'Territory level: CITY, PROVINCE, or COUNTRY';
COMMENT ON COLUMN user_territories.city_id IS 'City ID when level is CITY';
COMMENT ON COLUMN user_territories.province_id IS 'Province ID when level is PROVINCE';
COMMENT ON COLUMN user_territories.country_id IS 'Country ID when level is COUNTRY'; 