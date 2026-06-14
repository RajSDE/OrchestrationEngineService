package com.orchestrationengine.notification.step;

import com.orchestrationengine.notification.entity.NotificationTemplate;
import com.orchestrationengine.notification.repository.NotificationTemplateRepository;
import com.orchestrationengine.service.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common workflow step to resolve dynamic notification templates and send notifications.
 */
@Slf4j
@Component("notification.send")
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class NotificationSendStep implements WorkflowStep {

    private final NotificationTemplateRepository repository;

    @Override
    public void execute(Map<String, Object> context) throws Exception {
        String serviceCode = (String) context.get("serviceCode");
        String language = (String) context.get("language");
        if (language == null) {
            language = "en";
        }

        log.info("Executing notification.send for serviceCode: {} and language: {}", serviceCode, language);

        // Fetch template (fallback to English if not found for preferred language)
        Optional<NotificationTemplate> templateOpt = repository.findByServiceCodeAndLanguageAndTemplateType(serviceCode, language, "EMAIL");
        if (templateOpt.isEmpty() && !"en".equals(language)) {
            templateOpt = repository.findByServiceCodeAndLanguageAndTemplateType(serviceCode, "en", "EMAIL");
        }

        if (templateOpt.isEmpty()) {
            log.warn("No notification template found for serviceCode: {}", serviceCode);
            return;
        }

        NotificationTemplate template = templateOpt.get();
        String resolvedSubject = resolvePlaceholders(template.getSubject(), context);
        String resolvedBody = resolvePlaceholders(template.getTemplateBody(), context);

        // Simulate network/SMTP latency
        Thread.sleep(1000);

        log.info("Subject: {}", resolvedSubject);
        log.info("Body: {}", resolvedBody);

        context.put("notificationSent", true);
    }

    private String resolvePlaceholders(String templateStr, Map<String, Object> context) {
        if (templateStr == null) {
            return null;
        }
        Map<String, Object> request = (Map<String, Object>) context.get("request");
        String resolved = templateStr;
        Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(templateStr);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = null;
            if (request != null) {
                value = request.get(placeholder);
            }
            if (value == null) {
                value = context.get(placeholder);
            }
            String replacement = value != null ? value.toString() : "";
            resolved = resolved.replace("{" + placeholder + "}", replacement);
        }
        return resolved;
    }
}
