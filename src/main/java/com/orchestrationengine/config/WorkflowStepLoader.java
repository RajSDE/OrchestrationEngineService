package com.orchestrationengine.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowStepLoader {
    // Map serviceCode to XML file path
    private final Map<String, String> workflowMap = new HashMap<>();
    private boolean initialized = false;

    private void initWorkflowMap() {
        if (initialized) return;
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:*.xml");
            for (org.springframework.core.io.Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(is);
                    Element root = doc.getDocumentElement();
                    if (root != null && root.hasAttribute("id")) {
                        String id = root.getAttribute("id");
                        workflowMap.put(id, resource.getFilename());
                    }
                }
            }
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan workflow XML files", e);
        }
    }

    public List<String> loadStepsByServiceCode(String serviceCode) {
        initWorkflowMap();
        String xmlFilePath = workflowMap.get(serviceCode);
        if (xmlFilePath == null) {
            throw new RuntimeException("No workflow found for service code: " + serviceCode);
        }
        return loadSteps(xmlFilePath);
    }

    public List<String> loadSteps(String xmlFilePath) {
        List<String> steps = new ArrayList<>();
        try {
            InputStream is = new ClassPathResource(xmlFilePath).getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList stepNodes = doc.getElementsByTagName("step");
            for (int i = 0; i < stepNodes.getLength(); i++) {
                Element step = (Element) stepNodes.item(i);
                steps.add(step.getAttribute("id"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load steps from XML", e);
        }
        return steps;
    }
}
