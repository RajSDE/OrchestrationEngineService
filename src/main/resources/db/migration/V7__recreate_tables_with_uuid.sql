-- V7: Recreate tables with native UUID primary key and foreign keys
DROP TABLE IF EXISTS maindb.user_login_audit CASCADE;
DROP TABLE IF EXISTS maindb.user_credentials CASCADE;
DROP TABLE IF EXISTS maindb.user_profile CASCADE;

CREATE TABLE maindb.user_profile
(
    user_profile_id     UUID PRIMARY KEY,
    first_name          VARCHAR(100),
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100),
    full_name           VARCHAR(200),
    email               VARCHAR(255) UNIQUE NOT NULL,
    mobile_number       VARCHAR(20) UNIQUE,
    gender              VARCHAR(20),
    date_of_birth       DATE,
    profile_picture_url TEXT,
    preferred_language  VARCHAR(10),
    address_line1       TEXT,
    address_line2       TEXT,
    city                VARCHAR(100),
    state_or_province   VARCHAR(100),
    postal_code         VARCHAR(20),
    country             VARCHAR(100),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP
);

CREATE TABLE maindb.user_credentials
(
    id                  BIGSERIAL PRIMARY KEY,
    user_profile_id     UUID                NOT NULL,
    username            VARCHAR(150) UNIQUE NOT NULL,
    password_hash       TEXT                NOT NULL,
    password_changed_at TIMESTAMP,
    mfa_enabled         BOOLEAN   DEFAULT FALSE,
    mfa_type            VARCHAR(50),
    is_locked           BOOLEAN   DEFAULT FALSE,
    is_active           BOOLEAN   DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_profile_id)
        REFERENCES maindb.user_profile (user_profile_id) ON DELETE CASCADE
);

CREATE TABLE maindb.user_login_audit
(
    id                 BIGSERIAL PRIMARY KEY,
    user_profile_id    UUID        NOT NULL,
    login_attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_success         BOOLEAN     NOT NULL,
    ip_address         VARCHAR(45),
    user_agent         TEXT,
    failure_reason     TEXT,
    CONSTRAINT fk_user_login_audit_user FOREIGN KEY (user_profile_id)
        REFERENCES maindb.user_profile (user_profile_id) ON DELETE CASCADE
);
