-- V15: Make username column in user_credentials table nullable
ALTER TABLE maindb.user_credentials ALTER COLUMN username DROP NOT NULL;
