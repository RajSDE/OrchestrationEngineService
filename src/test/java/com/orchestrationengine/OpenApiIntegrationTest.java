package com.orchestrationengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testOpenApiEndpointAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        System.out.println("=== OPENAPI RESPONSE CODE: " + response.getStatusCode() + " ===");
        System.out.println("=== OPENAPI RESPONSE BODY: " + response.getBody() + " ===");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testRedocEndpointAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/docs", String.class);
        System.out.println("=== REDOC RESPONSE CODE: " + response.getStatusCode() + " ===");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
