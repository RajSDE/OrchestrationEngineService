-- V4: Add audit fields and update FK constraint for user_credentials table

-- Step 1: Add created_at and updated_at columns
ALTER TABLE user_credentials
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE user_credentials
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Step 2: Drop the existing foreign key constraint
ALTER TABLE user_credentials
DROP
CONSTRAINT user_credentials_user_id_fkey;

-- Step 3: Add new constraint with explicit name and schema-qualified reference
ALTER TABLE user_credentials
    ADD CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES maindb.user_profile (id)
            ON DELETE CASCADE;
