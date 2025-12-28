package com.orchestrationengine.config;

import com.orchestrationengine.model.WorkflowStepDefinition;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowStepLoader {
    // Changed Map to hold List<WorkflowStepDefinition>
    private final Map<String, List<WorkflowStepDefinition>> workflowStepsCache = new ConcurrentHashMap<>();

    @PostConstruct
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
                    List<WorkflowStepDefinition> steps = new ArrayList<>();

                    NodeList stepNodes = doc.getElementsByTagName("step");
                    for (int i = 0; i < stepNodes.getLength(); i++) {
                        Element step = (Element) stepNodes.item(i);

                        // Parse all attributes
                        steps.add(WorkflowStepDefinition.builder()
                                .id(step.getAttribute("id"))
                                .name(step.getAttribute("name"))
                                .retry(Boolean.parseBoolean(step.getAttribute("retry"))) // Default false if missing
                                .async(Boolean.parseBoolean(step.getAttribute("async")))
                                .rollback(Boolean.parseBoolean(step.getAttribute("rollback")))
                                .enabled(!step.hasAttribute("enable") || Boolean.parseBoolean(step.getAttribute("enable"))) // Default true
                                .build());
                    }
                    workflowStepsCache.put(serviceCode, steps);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load workflows", e);
        }
    }

    public List<WorkflowStepDefinition> getSteps(String serviceCode) {
        return workflowStepsCache.getOrDefault(serviceCode, new ArrayList<>());
    }
}