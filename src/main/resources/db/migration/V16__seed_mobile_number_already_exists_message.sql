-- V16: Seed English and Hindi translations for MOBILE_NUMBER_ALREADY_EXISTS

INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('USER_REGISTRATION', 'MOBILE_NUMBER_ALREADY_EXISTS', 'Mobile number already exists', 409, 'en'),
('USER_REGISTRATION', 'MOBILE_NUMBER_ALREADY_EXISTS', 'मोबाइल नंबर पहले से मौजूद है', 409, 'hi');
