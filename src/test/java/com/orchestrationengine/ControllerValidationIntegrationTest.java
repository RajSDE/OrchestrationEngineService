package com.orchestrationengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerValidationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRegistrationDtoValidationFailure() {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("username", "ab"); // too short (min 3)
        invalidPayload.put("email", "invalid-email"); // invalid email format
        // missing password, firstName, lastName

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(invalidPayload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/user/register", request, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("FAILED", body.get("status"));
        assertEquals("Validation failed", body.get("message"));

        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertEquals("VALIDATION", error.get("component"));
        assertEquals("INVALID_INPUT", error.get("code"));

        Map<?, ?> details = (Map<?, ?>) error.get("details");
        assertNotNull(details);
        assertTrue(details.containsKey("username"));
        assertTrue(details.containsKey("email"));
        assertTrue(details.containsKey("password"));
    }
}
