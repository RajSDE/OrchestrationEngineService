CREATE TABLE user_login_audit
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT  NOT NULL,
    login_attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_success         BOOLEAN NOT NULL,
    ip_address         VARCHAR(45),
    user_agent         TEXT,
    failure_reason     TEXT,
    FOREIGN KEY (user_id) REFERENCES user_profile (id) ON DELETE CASCADE
);
