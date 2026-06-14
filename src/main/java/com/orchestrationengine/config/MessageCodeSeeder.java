package com.orchestrationengine.config;

import com.orchestrationengine.model.MessageCode;
import com.orchestrationengine.repository.MessageCodeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCodeSeeder {

    private final MessageCodeRepository messageCodeRepository;

    @Value("${app.workflow.auto-seed-db:false}")
    private boolean autoSeedDb;

    @PostConstruct
    @Transactional
    public void seed() {
        if (!autoSeedDb) {
            return;
        }

        if (messageCodeRepository.count() > 0) {
            log.info("Message codes already exist in database, skipping seeding.");
            return;
        }

        log.info("Auto-seeding message_code lookup table for testing...");

        List<MessageCode> seedData = List.of(
            // USER_REGISTRATION (English)
            new MessageCode(null, "USER_REGISTRATION", "SUCCESS", "User registered successfully", 201, "en"),
            new MessageCode(null, "USER_REGISTRATION", "USERNAME_ALREADY_EXISTS", "Username already exists", 409, "en"),
            new MessageCode(null, "USER_REGISTRATION", "EMAIL_ALREADY_EXISTS", "Email already exists", 409, "en"),
            new MessageCode(null, "USER_REGISTRATION", "MOBILE_NUMBER_ALREADY_EXISTS", "Mobile number already exists", 409, "en"),
            new MessageCode(null, "USER_REGISTRATION", "INVALID_INPUT", "Invalid registration input", 400, "en"),

            // USER_REGISTRATION (Hindi)
            new MessageCode(null, "USER_REGISTRATION", "SUCCESS", "उपयोगकर्ता का पंजीकरण सफलतापूर्वक संपन्न हुआ", 201, "hi"),
            new MessageCode(null, "USER_REGISTRATION", "USERNAME_ALREADY_EXISTS", "उपयोगकर्ता नाम पहले से मौजूद है", 409, "hi"),
            new MessageCode(null, "USER_REGISTRATION", "EMAIL_ALREADY_EXISTS", "ईमेल पहले से मौजूद है", 409, "hi"),
            new MessageCode(null, "USER_REGISTRATION", "MOBILE_NUMBER_ALREADY_EXISTS", "मोबाइल नंबर पहले से मौजूद है", 409, "hi"),
            new MessageCode(null, "USER_REGISTRATION", "INVALID_INPUT", "अमान्य पंजीकरण इनपुट", 400, "hi"),

            // USER_LOGIN (English)
            new MessageCode(null, "USER_LOGIN", "SUCCESS", "Login successful", 200, "en"),
            new MessageCode(null, "USER_LOGIN", "INVALID_CREDENTIALS", "Invalid username or password", 401, "en"),
            new MessageCode(null, "USER_LOGIN", "ACCOUNT_DEACTIVATED", "This account has been deactivated", 401, "en"),
            new MessageCode(null, "USER_LOGIN", "ACCOUNT_LOCKED", "This account is locked", 401, "en"),
            new MessageCode(null, "USER_LOGIN", "INVALID_INPUT", "Username and password are required", 400, "en"),

            // USER_LOGIN (Hindi)
            new MessageCode(null, "USER_LOGIN", "SUCCESS", "लॉगिन सफल रहा", 200, "hi"),
            new MessageCode(null, "USER_LOGIN", "INVALID_CREDENTIALS", "अमान्य उपयोगकर्ता नाम या पासवर्ड", 401, "hi"),
            new MessageCode(null, "USER_LOGIN", "ACCOUNT_DEACTIVATED", "यह खाता निष्क्रिय कर दिया गया है", 401, "hi"),
            new MessageCode(null, "USER_LOGIN", "ACCOUNT_LOCKED", "यह खाता लॉक है", 401, "hi"),
            new MessageCode(null, "USER_LOGIN", "INVALID_INPUT", "उपयोगकर्ता नाम और पासवर्ड आवश्यक हैं", 400, "hi"),

            // DELETE_USER (English)
            new MessageCode(null, "DELETE_USER", "SUCCESS", "User deleted successfully", 200, "en"),
            new MessageCode(null, "DELETE_USER", "USER_NOT_FOUND", "No user found with the specified ID", 404, "en"),
            new MessageCode(null, "DELETE_USER", "INVALID_USER_ID", "Invalid user ID format", 400, "en"),
            new MessageCode(null, "DELETE_USER", "INVALID_INPUT", "User ID is required", 400, "en"),

            // DELETE_USER (Hindi)
            new MessageCode(null, "DELETE_USER", "SUCCESS", "उपयोगकर्ता को सफलतापूर्वक हटा दिया गया", 200, "hi"),
            new MessageCode(null, "DELETE_USER", "USER_NOT_FOUND", "निर्दिष्ट आईडी के साथ कोई उपयोगकर्ता नहीं मिला", 404, "hi"),
            new MessageCode(null, "DELETE_USER", "INVALID_USER_ID", "अमान्य उपयोगकर्ता आईडी प्रारूप", 400, "hi"),
            new MessageCode(null, "DELETE_USER", "INVALID_INPUT", "उपयोगकर्ता आईडी आवश्यक है", 400, "hi"),

            // DEACTIVATE_USER (English)
            new MessageCode(null, "DEACTIVATE_USER", "SUCCESS", "User deactivated successfully", 200, "en"),
            new MessageCode(null, "DEACTIVATE_USER", "USER_NOT_FOUND", "Credentials not found for the specified user ID", 404, "en"),
            new MessageCode(null, "DEACTIVATE_USER", "INVALID_USER_ID", "Invalid user ID format", 400, "en"),
            new MessageCode(null, "DEACTIVATE_USER", "INVALID_INPUT", "User ID is required", 400, "en"),

            // DEACTIVATE_USER (Hindi)
            new MessageCode(null, "DEACTIVATE_USER", "SUCCESS", "उपयोगकर्ता को सफलतापूर्वक निष्क्रिय कर दिया गया", 200, "hi"),
            new MessageCode(null, "DEACTIVATE_USER", "USER_NOT_FOUND", "निर्दिष्ट उपयोगकर्ता आईडी के लिए क्रेडेंशियल नहीं मिले", 404, "hi"),
            new MessageCode(null, "DEACTIVATE_USER", "INVALID_USER_ID", "अमान्य उपयोगकर्ता आईडी प्रारूप", 400, "hi"),
            new MessageCode(null, "DEACTIVATE_USER", "INVALID_INPUT", "उपयोगकर्ता आईडी आवश्यक है", 400, "hi"),

            // FORGOT_PASSWORD (English)
            new MessageCode(null, "FORGOT_PASSWORD", "SUCCESS", "Password reset notification sent successfully", 200, "en"),
            new MessageCode(null, "FORGOT_PASSWORD", "EMAIL_NOT_FOUND", "No user found with this email", 404, "en"),
            new MessageCode(null, "FORGOT_PASSWORD", "INVALID_INPUT", "Email is required", 400, "en"),

            // FORGOT_PASSWORD (Hindi)
            new MessageCode(null, "FORGOT_PASSWORD", "SUCCESS", "पासवर्ड रीसेट सूचना सफलतापूर्वक भेजी गई", 200, "hi"),
            new MessageCode(null, "FORGOT_PASSWORD", "EMAIL_NOT_FOUND", "इस ईमेल के साथ कोई उपयोगकर्ता नहीं मिला", 404, "hi"),
            new MessageCode(null, "FORGOT_PASSWORD", "INVALID_INPUT", "ईमेल आवश्यक है", 400, "hi"),

            // RESET_PASSWORD (English)
            new MessageCode(null, "RESET_PASSWORD", "SUCCESS", "Password updated successfully", 200, "en"),
            new MessageCode(null, "RESET_PASSWORD", "INVALID_TOKEN", "Reset token is invalid or does not exist", 400, "en"),
            new MessageCode(null, "RESET_PASSWORD", "TOKEN_ALREADY_USED", "This reset token has already been used", 400, "en"),
            new MessageCode(null, "RESET_PASSWORD", "TOKEN_EXPIRED", "This reset token has expired", 400, "en"),
            new MessageCode(null, "RESET_PASSWORD", "CREDENTIALS_NOT_FOUND", "Credentials not found for the user profile", 400, "en"),
            new MessageCode(null, "RESET_PASSWORD", "INVALID_INPUT", "New password and token are required", 400, "en"),

            // RESET_PASSWORD (Hindi)
            new MessageCode(null, "RESET_PASSWORD", "SUCCESS", "पासवर्ड सफलतापूर्वक अपडेट किया गया", 200, "hi"),
            new MessageCode(null, "RESET_PASSWORD", "INVALID_TOKEN", "रीसेट टोकन अमान्य है या मौजूद नहीं है", 400, "hi"),
            new MessageCode(null, "RESET_PASSWORD", "TOKEN_ALREADY_USED", "इस रीसेट टोकन का पहले ही उपयोग किया जा चुका है", 400, "hi"),
            new MessageCode(null, "RESET_PASSWORD", "TOKEN_EXPIRED", "इस रीसेट टोकन की अवधि समाप्त हो गई है", 400, "hi"),
            new MessageCode(null, "RESET_PASSWORD", "CREDENTIALS_NOT_FOUND", "उपयोगकर्ता प्रोफ़ाइल के लिए क्रेडेंशियल नहीं मिले", 400, "hi"),
            new MessageCode(null, "RESET_PASSWORD", "INVALID_INPUT", "नया पासवर्ड और टोकन आवश्यक हैं", 400, "hi")
        );

        messageCodeRepository.saveAll(seedData);
        log.info("Successfully seeded {} message codes.", seedData.size());
    }
}
