-- V13: Add language column and Hindi translations to message_code table

ALTER TABLE maindb.message_code ADD COLUMN language VARCHAR(10) DEFAULT 'en' NOT NULL;

-- Drop old service-code unique constraint
DROP INDEX IF EXISTS maindb.uq_service_error_code;

-- Create new unique index including language
CREATE UNIQUE INDEX uq_service_code_language ON maindb.message_code (service_code, code, language);

-- Seed Hindi translations for USER_REGISTRATION
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('USER_REGISTRATION', 'SUCCESS', 'उपयोगकर्ता का पंजीकरण सफलतापूर्वक संपन्न हुआ', 201, 'hi'),
('USER_REGISTRATION', 'USERNAME_ALREADY_EXISTS', 'उपयोगकर्ता नाम पहले से मौजूद है', 409, 'hi'),
('USER_REGISTRATION', 'EMAIL_ALREADY_EXISTS', 'ईमेल पहले से मौजूद है', 409, 'hi'),
('USER_REGISTRATION', 'INVALID_INPUT', 'अमान्य पंजीकरण इनपुट', 400, 'hi');
