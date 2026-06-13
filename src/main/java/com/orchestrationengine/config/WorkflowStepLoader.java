package com.orchestrationengine.config;

import com.orchestrationengine.model.WorkflowStepDefinition;
import com.orchestrationengine.repository.WorkflowRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches step sequences from XML configurations on startup.
 * Enforces a fail-fast startup verification by validating that all scanned XML service codes
 * are pre-registered/mapped in the database "workflows" table.
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@RequiredArgsConstructor
public class WorkflowStepLoader {

    private final WorkflowRepository workflowRepository;
    private final Map<String, List<WorkflowStepDefinition>> workflowStepsCache = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${app.workflow.auto-seed-db:false}")
    private boolean autoSeedDb;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:workflows/*.xml");

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(is);
                    Element root = doc.getDocumentElement();

                    String serviceCode = root.getAttribute("serviceCode");
                    if (serviceCode == null || serviceCode.trim().isEmpty()) {
                        continue;
                    }

                    // Validate that the service code is registered/mapped in the database
                    if (!workflowRepository.existsById(serviceCode)) {
                        if (autoSeedDb) {
                            log.info("Auto-seeding workflow service code '{}' into database for testing", serviceCode);
                            com.orchestrationengine.model.Workflow workflow = com.orchestrationengine.model.Workflow.builder()
                                    .serviceCode(serviceCode)
                                    .name(serviceCode + " Workflow")
                                    .enabled("Y")
                                    .build();
                            workflowRepository.save(workflow);
                        } else {
                            String errMsg = "CRITICAL STARTUP ERROR: Workflow service code '" + serviceCode + "' is not registered/mapped in the database workflows table!";
                            log.error(errMsg);
                            throw new RuntimeException(errMsg);
                        }
                    }

                    log.info("Loading workflow configuration for registered service: {}", serviceCode);
                    List<WorkflowStepDefinition> steps = new ArrayList<>();
                    NodeList stepNodes = doc.getElementsByTagName("step");
                    for (int i = 0; i < stepNodes.getLength(); i++) {
                        Element step = (Element) stepNodes.item(i);
                        long timeout = step.hasAttribute("timeout") && !step.getAttribute("timeout").isEmpty()
                                ? Long.parseLong(step.getAttribute("timeout")) : 10000L;

                        steps.add(WorkflowStepDefinition.builder()
                                .id(step.getAttribute("id"))
                                .name(step.getAttribute("name"))
                                .retry(Boolean.parseBoolean(step.getAttribute("retry")))
                                .async(Boolean.parseBoolean(step.getAttribute("async")))
                                .rollback(Boolean.parseBoolean(step.getAttribute("rollback")))
                                .timeout(timeout)
                                .enabled(!step.hasAttribute("enable") || Boolean.parseBoolean(step.getAttribute("enable")))
                                .build());
                    }
                    workflowStepsCache.put(serviceCode, steps);
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load and validate workflow configurations at startup", e);
        }
    }

    public List<WorkflowStepDefinition> getSteps(String serviceCode) {
        return workflowStepsCache.getOrDefault(serviceCode, new ArrayList<>());
    }
}