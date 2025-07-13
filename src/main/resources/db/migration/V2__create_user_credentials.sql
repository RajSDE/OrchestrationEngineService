CREATE TABLE user_credentials
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT              NOT NULL,
    username            VARCHAR(150) UNIQUE NOT NULL,
    password_hash       TEXT                NOT NULL,
    password_changed_at TIMESTAMP,
    mfa_enabled         BOOLEAN DEFAULT FALSE,
    mfa_type            VARCHAR(50),
    is_locked           BOOLEAN DEFAULT FALSE,
    is_active           BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES user_profile (id) ON DELETE CASCADE
);
