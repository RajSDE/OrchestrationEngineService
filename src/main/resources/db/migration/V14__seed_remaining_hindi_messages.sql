-- V14: Seed Hindi translations for all remaining service codes in message_code table

-- Seed Hindi translations for USER_LOGIN
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('USER_LOGIN', 'SUCCESS', 'लॉगिन सफल रहा', 200, 'hi'),
('USER_LOGIN', 'INVALID_CREDENTIALS', 'अमान्य उपयोगकर्ता नाम या पासवर्ड', 401, 'hi'),
('USER_LOGIN', 'ACCOUNT_DEACTIVATED', 'यह खाता निष्क्रिय कर दिया गया है', 401, 'hi'),
('USER_LOGIN', 'ACCOUNT_LOCKED', 'यह खाता लॉक है', 401, 'hi'),
('USER_LOGIN', 'INVALID_INPUT', 'उपयोगकर्ता नाम और पासवर्ड आवश्यक हैं', 400, 'hi');

-- Seed Hindi translations for DELETE_USER
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('DELETE_USER', 'SUCCESS', 'उपयोगकर्ता को सफलतापूर्वक हटा दिया गया', 200, 'hi'),
('DELETE_USER', 'USER_NOT_FOUND', 'निर्दिष्ट आईडी के साथ कोई उपयोगकर्ता नहीं मिला', 404, 'hi'),
('DELETE_USER', 'INVALID_USER_ID', 'अमान्य उपयोगकर्ता आईडी प्रारूप', 400, 'hi'),
('DELETE_USER', 'INVALID_INPUT', 'उपयोगकर्ता आईडी आवश्यक है', 400, 'hi');

-- Seed Hindi translations for DEACTIVATE_USER
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('DEACTIVATE_USER', 'SUCCESS', 'उपयोगकर्ता को सफलतापूर्वक निष्क्रिय कर दिया गया', 200, 'hi'),
('DEACTIVATE_USER', 'USER_NOT_FOUND', 'निर्दिष्ट उपयोगकर्ता आईडी के लिए क्रेडेंशियल नहीं मिले', 404, 'hi'),
('DEACTIVATE_USER', 'INVALID_USER_ID', 'अमान्य उपयोगकर्ता आईडी प्रारूप', 400, 'hi'),
('DEACTIVATE_USER', 'INVALID_INPUT', 'उपयोगकर्ता आईडी आवश्यक है', 400, 'hi');

-- Seed Hindi translations for FORGOT_PASSWORD
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('FORGOT_PASSWORD', 'SUCCESS', 'पासवर्ड रीसेट सूचना सफलतापूर्वक भेजी गई', 200, 'hi'),
('FORGOT_PASSWORD', 'EMAIL_NOT_FOUND', 'इस ईमेल के साथ कोई उपयोगकर्ता नहीं मिला', 404, 'hi'),
('FORGOT_PASSWORD', 'INVALID_INPUT', 'ईमेल आवश्यक है', 400, 'hi');

-- Seed Hindi translations for RESET_PASSWORD
INSERT INTO maindb.message_code (service_code, code, message, http_code, language) VALUES
('RESET_PASSWORD', 'SUCCESS', 'पासवर्ड सफलतापूर्वक अपडेट किया गया', 200, 'hi'),
('RESET_PASSWORD', 'INVALID_TOKEN', 'रीसेट टोकन अमान्य है या मौजूद नहीं है', 400, 'hi'),
('RESET_PASSWORD', 'TOKEN_ALREADY_USED', 'इस रीसेट टोकन का पहले ही उपयोग किया जा चुका है', 400, 'hi'),
('RESET_PASSWORD', 'TOKEN_EXPIRED', 'इस रीसेट टोकन की अवधि समाप्त हो गई है', 400, 'hi'),
('RESET_PASSWORD', 'CREDENTIALS_NOT_FOUND', 'उपयोगकर्ता प्रोफ़ाइल के लिए क्रेडेंशियल नहीं मिले', 400, 'hi'),
('RESET_PASSWORD', 'INVALID_INPUT', 'नया पासवर्ड और टोकन आवश्यक हैं', 400, 'hi');
