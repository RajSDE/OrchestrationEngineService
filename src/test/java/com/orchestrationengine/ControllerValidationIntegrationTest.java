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
        assertEquals("Invalid registration input", body.get("message"));

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
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        assertNotNull(body.get("timestamp"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("USERNAME_ALREADY_EXISTS", error.get("code"));
        assertEquals("validate.user", error.get("component"));
        assertEquals("Username already exists", error.get("message"));
    }

    @Test
    public void testRegistrationDuplicateUsernameFailureHindi() {
        String baseUser = "dupUserHindi_" + System.currentTimeMillis();
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

        // Try registering again with Accept-Language: hi
        Map<String, Object> dupPayload = new HashMap<>(payload);
        dupPayload.put("email", "different_" + baseUser + "@example.com");
        
        HttpHeaders hiHeaders = new HttpHeaders();
        hiHeaders.setContentType(MediaType.APPLICATION_JSON);
        hiHeaders.add("Accept-Language", "hi");
        HttpEntity<Map<String, Object>> dupRequest = new HttpEntity<>(dupPayload, hiHeaders);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/v1/user/register", dupRequest, Map.class);
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("USERNAME_ALREADY_EXISTS", error.get("code"));
        assertEquals("उपयोगकर्ता नाम पहले से मौजूद है", error.get("message"));
    }

    @Test
    public void testRegistrationDuplicateUsernameFailureLanguageFallback() {
        String baseUser = "dupUserFall_" + System.currentTimeMillis();
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

        // Try registering again with Accept-Language: fr (should fallback to en)
        Map<String, Object> dupPayload = new HashMap<>(payload);
        dupPayload.put("email", "different_" + baseUser + "@example.com");
        
        HttpHeaders frHeaders = new HttpHeaders();
        frHeaders.setContentType(MediaType.APPLICATION_JSON);
        frHeaders.add("Accept-Language", "fr");
        HttpEntity<Map<String, Object>> dupRequest = new HttpEntity<>(dupPayload, frHeaders);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/v1/user/register", dupRequest, Map.class);
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("USERNAME_ALREADY_EXISTS", error.get("code"));
        assertEquals("Username already exists", error.get("message"));
    }

    @Test
    public void testRegistrationOptionalUsername() {
        String baseUser = "optUser_" + System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        // Omit username
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Optional");
        payload.put("lastName", "Username");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("SUCCESS", body.get("status"));
        assertNull(body.get("username"));
    }

    @Test
    public void testRegistrationBlankUsernameSucceeds() {
        String baseUser = "blankUser_" + System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "   "); // blank spaces
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Blank");
        payload.put("lastName", "Username");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("SUCCESS", body.get("status"));
        assertNull(body.get("username"));
    }

    @Test
    public void testRegistrationDtoValidationFailureHindi() {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("username", "ab"); // too short (min 3)
        invalidPayload.put("email", "invalid-email");
        invalidPayload.put("password", "Pass12345");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept-Language", "hi");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(invalidPayload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/user/register", request, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("FAILED", body.get("status"));
        assertEquals("अमान्य पंजीकरण इनपुट", body.get("message"));

        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertEquals("VALIDATION", error.get("component"));
        assertEquals("INVALID_INPUT", error.get("code"));
    }

    @Test
    public void testRegistrationDuplicateMobileNumberFailure() {
        String baseUser = "mobUser_" + System.currentTimeMillis();
        String mobile = "+199999" + (System.currentTimeMillis() % 10000);
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", baseUser);
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Mob");
        payload.put("lastName", "User");
        payload.put("mobileNumber", mobile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Try registering again with different username and email but same mobile number
        Map<String, Object> dupPayload = new HashMap<>();
        dupPayload.put("username", baseUser + "_dup");
        dupPayload.put("email", baseUser + "_dup@example.com");
        dupPayload.put("password", "Pass12345");
        dupPayload.put("firstName", "Mob");
        dupPayload.put("lastName", "User");
        dupPayload.put("mobileNumber", mobile);

        HttpEntity<Map<String, Object>> dupRequest = new HttpEntity<>(dupPayload, headers);
        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/v1/user/register", dupRequest, Map.class);

        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("MOBILE_NUMBER_ALREADY_EXISTS", error.get("code"));
        assertEquals("Mobile number already exists", error.get("message"));
    }

    @Test
    public void testRegistrationDuplicateMobileNumberFailureHindi() {
        String baseUser = "mobUserHi_" + System.currentTimeMillis();
        String mobile = "+199888" + (System.currentTimeMillis() % 10000);
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", baseUser);
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Mob");
        payload.put("lastName", "User");
        payload.put("mobileNumber", mobile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Try registering again with different username and email but same mobile number and Accept-Language: hi
        Map<String, Object> dupPayload = new HashMap<>();
        dupPayload.put("username", baseUser + "_dup");
        dupPayload.put("email", baseUser + "_dup@example.com");
        dupPayload.put("password", "Pass12345");
        dupPayload.put("firstName", "Mob");
        dupPayload.put("lastName", "User");
        dupPayload.put("mobileNumber", mobile);

        HttpHeaders hiHeaders = new HttpHeaders();
        hiHeaders.setContentType(MediaType.APPLICATION_JSON);
        hiHeaders.add("Accept-Language", "hi");

        HttpEntity<Map<String, Object>> dupRequest = new HttpEntity<>(dupPayload, hiHeaders);
        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/v1/user/register", dupRequest, Map.class);

        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());

        Map<?, ?> body = secondResponse.getBody();
        assertEquals("FAILED", body.get("status"));
        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("MOBILE_NUMBER_ALREADY_EXISTS", error.get("code"));
        assertEquals("मोबाइल नंबर पहले से मौजूद है", error.get("message"));
    }

    @Test
    public void testMalformedJsonReturnsFriendlyError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{ \"username\": }", headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/user/register", request, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("FAILED", body.get("status"));
        assertEquals("Malformed JSON request body", body.get("message"));

        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("VALIDATION", error.get("component"));
        assertEquals("INVALID_INPUT", error.get("code"));
        assertEquals("Malformed JSON request body or missing parameter", error.get("message"));
    }

    @Test
    public void testMethodNotSupportedReturnsFriendlyError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        // GET request to a POST-only endpoint
        ResponseEntity<Map> response = restTemplate.exchange("/v1/user/register", HttpMethod.GET, request, Map.class);
        
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("FAILED", body.get("status"));
        assertTrue(body.get("message").toString().contains("Request method 'GET' is not supported"));

        Map<?, ?> error = (Map<?, ?>) body.get("error");
        assertNotNull(error);
        assertEquals("SYSTEM", error.get("component"));
        assertEquals("BAD_REQUEST", error.get("code"));
    }

    @Test
    public void testWorkflowControllerRequestResponsePreservation() {
        String baseUser = "wfUser_" + System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", baseUser);
        payload.put("email", baseUser + "@example.com");
        payload.put("password", "Pass12345");
        payload.put("firstName", "Wf");
        payload.put("lastName", "User");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/serviceflow/USER_REGISTRATION", request, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = response.getBody();
        assertEquals("SUCCESS", body.get("status"));

        // Verify request key contains the original input payload keys
        Map<?, ?> preservedRequest = (Map<?, ?>) body.get("request");
        assertNotNull(preservedRequest);
        assertEquals(baseUser, preservedRequest.get("username"));
        assertEquals(baseUser + "@example.com", preservedRequest.get("email"));

        // Verify response key is present
        assertNotNull(body.get("response"));
    }
}
