-- V8: Create workflow configuration and authentication schema tables

-- Workflow configuration table
CREATE TABLE maindb.workflow_definition
(
    service_code VARCHAR(100) PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    is_active    BOOLEAN DEFAULT TRUE
);

-- Workflow step configuration table
CREATE TABLE maindb.workflow_step_configuration
(
    id              BIGSERIAL PRIMARY KEY,
    service_code    VARCHAR(100) NOT NULL,
    step_id         VARCHAR(100) NOT NULL,
    step_name       VARCHAR(200) NOT NULL,
    retry           BOOLEAN DEFAULT FALSE,
    async           BOOLEAN DEFAULT FALSE,
    rollback        BOOLEAN DEFAULT FALSE,
    timeout         BIGINT DEFAULT 10000,
    enabled         BOOLEAN DEFAULT TRUE,
    execution_order INT NOT NULL,
    CONSTRAINT fk_step_config_workflow FOREIGN KEY (service_code)
        REFERENCES maindb.workflow_definition (service_code) ON DELETE CASCADE
);

-- User auth session management table
CREATE TABLE maindb.user_auth
(
    id                   BIGSERIAL PRIMARY KEY,
    user_profile_id      UUID                NOT NULL,
    refresh_token        VARCHAR(255) UNIQUE NOT NULL,
    refresh_token_expiry TIMESTAMP           NOT NULL,
    is_revoked           BOOLEAN   DEFAULT FALSE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_auth_user FOREIGN KEY (user_profile_id)
        REFERENCES maindb.user_profile (user_profile_id) ON DELETE CASCADE
);

-- Password reset tokens management table
CREATE TABLE maindb.password_reset_token
(
    id              BIGSERIAL PRIMARY KEY,
    user_profile_id UUID                NOT NULL,
    reset_token     VARCHAR(255) UNIQUE NOT NULL,
    expiry_time     TIMESTAMP           NOT NULL,
    is_used         BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_profile_id)
        REFERENCES maindb.user_profile (user_profile_id) ON DELETE CASCADE
);
