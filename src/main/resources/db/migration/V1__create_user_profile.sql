CREATE TABLE user_profile
(
    id                  BIGSERIAL PRIMARY KEY,
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
