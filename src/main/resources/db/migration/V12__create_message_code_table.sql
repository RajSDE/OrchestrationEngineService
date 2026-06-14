-- V12: Create message_code table for success and error localization/mapping

CREATE TABLE maindb.message_code
(
    id           SERIAL PRIMARY KEY,
    service_code VARCHAR(100) NOT NULL,
    code         VARCHAR(100) NOT NULL,
    message      TEXT NOT NULL,
    http_code    INT NOT NULL
);

CREATE UNIQUE INDEX uq_service_error_code ON maindb.message_code (service_code, code);

-- Seed USER_REGISTRATION
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('USER_REGISTRATION', 'SUCCESS', 'User registered successfully', 201),
('USER_REGISTRATION', 'USERNAME_ALREADY_EXISTS', 'Username already exists', 409),
('USER_REGISTRATION', 'EMAIL_ALREADY_EXISTS', 'Email already exists', 409),
('USER_REGISTRATION', 'INVALID_INPUT', 'Invalid registration input', 400);

-- Seed USER_LOGIN
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('USER_LOGIN', 'SUCCESS', 'Login successful', 200),
('USER_LOGIN', 'INVALID_CREDENTIALS', 'Invalid username or password', 401),
('USER_LOGIN', 'ACCOUNT_DEACTIVATED', 'This account has been deactivated', 401),
('USER_LOGIN', 'ACCOUNT_LOCKED', 'This account is locked', 401),
('USER_LOGIN', 'INVALID_INPUT', 'Username and password are required', 400);

-- Seed DELETE_USER
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('DELETE_USER', 'SUCCESS', 'User deleted successfully', 200),
('DELETE_USER', 'USER_NOT_FOUND', 'No user found with the specified ID', 404),
('DELETE_USER', 'INVALID_USER_ID', 'Invalid user ID format', 400),
('DELETE_USER', 'INVALID_INPUT', 'User ID is required', 400);

-- Seed DEACTIVATE_USER
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('DEACTIVATE_USER', 'SUCCESS', 'User deactivated successfully', 200),
('DEACTIVATE_USER', 'USER_NOT_FOUND', 'Credentials not found for the specified user ID', 404),
('DEACTIVATE_USER', 'INVALID_USER_ID', 'Invalid user ID format', 400),
('DEACTIVATE_USER', 'INVALID_INPUT', 'User ID is required', 400);

-- Seed FORGOT_PASSWORD
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('FORGOT_PASSWORD', 'SUCCESS', 'Password reset notification sent successfully', 200),
('FORGOT_PASSWORD', 'EMAIL_NOT_FOUND', 'No user found with this email', 404),
('FORGOT_PASSWORD', 'INVALID_INPUT', 'Email is required', 400);

-- Seed RESET_PASSWORD
INSERT INTO maindb.message_code (service_code, code, message, http_code) VALUES
('RESET_PASSWORD', 'SUCCESS', 'Password updated successfully', 200),
('RESET_PASSWORD', 'INVALID_TOKEN', 'Reset token is invalid or does not exist', 400),
('RESET_PASSWORD', 'TOKEN_ALREADY_USED', 'This reset token has already been used', 400),
('RESET_PASSWORD', 'TOKEN_EXPIRED', 'This reset token has expired', 400),
('RESET_PASSWORD', 'CREDENTIALS_NOT_FOUND', 'Credentials not found for the user profile', 400),
('RESET_PASSWORD', 'INVALID_INPUT', 'New password and token are required', 400);
