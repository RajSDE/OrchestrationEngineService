-- V9: Drop obsolete workflow tables, alter token columns to TEXT, and create workflows table

-- Drop obsolete tables created in V8
DROP TABLE IF EXISTS maindb.workflow_step_configuration CASCADE;
DROP TABLE IF EXISTS maindb.workflow_definition CASCADE;

-- Alter column types for token storage to TEXT in user_auth and password_reset_token to support longer strings (e.g. JWT tokens)
ALTER TABLE maindb.user_auth ALTER COLUMN refresh_token TYPE TEXT;
ALTER TABLE maindb.password_reset_token ALTER COLUMN reset_token TYPE TEXT;

-- Workflows metadata and runtime toggle table
CREATE TABLE maindb.workflows
(
    service_code VARCHAR(100) PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    enabled      VARCHAR(1) DEFAULT 'Y',
    created_on   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_on  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(100) DEFAULT 'SYSTEM',
    modified_by  VARCHAR(100) DEFAULT 'SYSTEM'
);

-- Seed default workflows in workflows table to satisfy startup validation
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('USER_REGISTRATION', 'User Registration Workflow', 'Y');
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('USER_LOGIN', 'User Login Workflow', 'Y');
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('FORGOT_PASSWORD', 'Forgot Password Workflow', 'Y');
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('RESET_PASSWORD', 'Reset Password Workflow', 'Y');
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('DELETE_USER', 'Delete User Workflow', 'Y');
INSERT INTO maindb.workflows (service_code, name, enabled) VALUES ('DEACTIVATE_USER', 'Deactivate User Workflow', 'Y');
