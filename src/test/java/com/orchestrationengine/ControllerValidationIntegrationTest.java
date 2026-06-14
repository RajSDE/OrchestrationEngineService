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

    @Test
    public void testRegistrationDuplicateUsernameFailure() {
        String baseUser = "dupUser_" + System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", baseUser);
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Dup");
        payload.put("lastName", "User");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Try registering again with the same username but different email
        Map<String, Object> dupPayload = new HashMap<>(payload);
        dupPayload.put("email", "different_" + baseUser + "@example.com");
        HttpEntity<Map<String, Object>> dupRequest = new HttpEntity<>(dupPayload, headers);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/v1/user/register", dupRequest, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        assertNotNull(body.get("timestamp"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("USERNAME_ALREADY_EXISTS", error.get("code"));
        assertEquals("validate.user", error.get("component"));
    }
}
