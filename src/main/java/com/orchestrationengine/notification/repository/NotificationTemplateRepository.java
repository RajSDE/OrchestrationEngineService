package com.orchestrationengine.notification.repository;

import com.orchestrationengine.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {
    Optional<NotificationTemplate> findByServiceCodeAndLanguageAndTemplateType(String serviceCode, String language, String templateType);
}
