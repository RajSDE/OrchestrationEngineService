package com.orchestrationengine;

import com.orchestrationengine.repository.ServiceRequestRepository;
import com.orchestrationengine.service.WorkflowExecutor;
import com.orchestrationengine.ums.entity.ServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServiceRequestAuditIntegrationTest {

    @Autowired
    private WorkflowExecutor workflowExecutor;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @BeforeEach
    public void setup() {
        serviceRequestRepository.deleteAll();
    }

    @Test
    public void testAuditLoggingAndRedaction() {
        // 1. Execute USER_REGISTRATION workflow (should be logged based on application.properties)
        Map<String, Object> regCtx = new ConcurrentHashMap<>();
        regCtx.put("username", "audituser");
        regCtx.put("email", "audit@example.com");
        regCtx.put("password", "SecretPassword456");
        regCtx.put("firstName", "Audit");
        regCtx.put("lastName", "User");

        workflowExecutor.executeWorkflowByServiceCode("USER_REGISTRATION", regCtx, "en");

        // Assert audit entry is created
        List<ServiceRequest> requests = serviceRequestRepository.findAll();
        assertEquals(1, requests.size());

        ServiceRequest request = requests.get(0);
        assertEquals("USER_REGISTRATION", request.getServiceCode());
        assertEquals("SUCCESS", request.getStatus());
        assertNotNull(request.getTraceId());
        assertTrue(request.getDurationMs() >= 0);

        // Assert password redaction in recorded request payload
        String reqPayloadJson = request.getRequestPayload();
        assertNotNull(reqPayloadJson);
        assertFalse(reqPayloadJson.contains("SecretPassword456"), "Password must not be logged in plaintext");
        assertFalse(reqPayloadJson.contains("\"password\""), "Password key must be redacted");

        // 2. Execute USER_LOGIN workflow (should NOT be logged based on application.properties)
        Map<String, Object> loginCtx = new ConcurrentHashMap<>();
        loginCtx.put("username", "audituser");
        loginCtx.put("password", "SecretPassword456");

        workflowExecutor.executeWorkflowByServiceCode("USER_LOGIN", loginCtx, "en");

        // Assert no new audit entry is created (should remain 1)
        List<ServiceRequest> requestsAfterLogin = serviceRequestRepository.findAll();
        assertEquals(1, requestsAfterLogin.size(), "USER_LOGIN must not be recorded");
    }
}
