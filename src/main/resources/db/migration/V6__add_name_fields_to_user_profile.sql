-- V6: Add name fields to user_profile table
ALTER TABLE maindb.user_profile ADD COLUMN first_name VARCHAR(100);
ALTER TABLE maindb.user_profile ADD COLUMN middle_name VARCHAR(100);
ALTER TABLE maindb.user_profile ADD COLUMN last_name VARCHAR(100);
