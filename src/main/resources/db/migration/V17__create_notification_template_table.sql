-- V17: Create notification_template table and seed default templates
CREATE TABLE maindb.notification_template (
    id SERIAL PRIMARY KEY,
    service_code VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) DEFAULT 'EMAIL',
    subject VARCHAR(255),
    template_body TEXT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_language_type UNIQUE (service_code, language, template_type)
);

INSERT INTO maindb.notification_template (service_code, template_type, subject, template_body, language) VALUES
('USER_REGISTRATION', 'EMAIL', 'Welcome to our service!', 'Hello {firstName} {lastName}, thank you for registering with username: {username} and email: {email}.', 'en'),
('USER_REGISTRATION', 'EMAIL', 'हमारे सेवा में आपका स्वागत है!', 'नमस्ते {firstName} {lastName}, उपयोगकर्ता नाम: {username} और ईमेल: {email} के साथ पंजीकरण करने के लिए धन्यवाद।', 'hi'),
('FORGOT_PASSWORD', 'EMAIL', 'Password Reset Request', 'Hello, you requested a password reset. Use link http://localhost:8080/v1/user/reset-password?token={resetToken} to reset your password.', 'en'),
('FORGOT_PASSWORD', 'EMAIL', 'पासवर्ड रीसेट अनुरोध', 'नमस्ते, आपने पासवर्ड रीसेट का अनुरोध किया है। अपना पासवर्ड रीसेट करने के लिए लिंक http://localhost:8080/v1/user/reset-password?token={resetToken} का उपयोग करें।', 'hi');
