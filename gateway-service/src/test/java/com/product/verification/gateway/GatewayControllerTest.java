package com.product.verification.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GatewayControllerTest {

    private GatewayController gatewayController;
    private RestTemplate mockRestTemplate;
    private static final String INVENTORY_URL = "http://localhost:8081/internal/check/";
    private static final String INVENTORY_KEY = "internal-secret-key";

    @BeforeEach
    void setUp() {
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        gatewayController = new GatewayController(mockRestTemplate);
        ReflectionTestUtils.setField(gatewayController, "inventoryServiceUrl", INVENTORY_URL);
        ReflectionTestUtils.setField(gatewayController, "inventoryServiceKey", INVENTORY_KEY);
    }

    @Test
    void getPublicProduct_Success() {
        Object mockProduct = new Object();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockProduct, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                eq(INVENTORY_URL + "1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<?> response = gatewayController.getPublicProduct("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProduct, response.getBody());
    }

    @Test
    void getSecureProduct_Success() {
        Object mockProduct = new Object();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockProduct, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                eq(INVENTORY_URL + "2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<?> response = gatewayController.getSecureProduct("2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProduct, response.getBody());
    }

    @Test
    void processRequest_InvalidId() {
        ResponseEntity<?> response = gatewayController.getPublicProduct("abc");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void processRequest_InvalidId_Negative() {
        ResponseEntity<?> response = gatewayController.getPublicProduct("-1");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid product ID. Must be a positive number.", response.getBody());
    }

    @Test
    void processRequest_NotFound() {
        String errorMessage = "Product not found with id: 99";
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(), errorMessage.getBytes(), null));

        ResponseEntity<?> response = gatewayController.getPublicProduct("99");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void processRequest_Forbidden() {
        String errorMessage = "Forbidden: Invalid Service Key";
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", new HttpHeaders(), errorMessage.getBytes(), null));

        ResponseEntity<?> response = gatewayController.getPublicProduct("1");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void processRequest_ServiceUnavailable() {
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        try {
            gatewayController.getPublicProduct("1");
        } catch (HttpServerErrorException e) {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }
    }

    @Test
    void processRequest_OtherServerError() {
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            gatewayController.getPublicProduct("1");
        } catch (HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
        }
    }

    @Test
    void processRequest_ServiceUnavailable_RetriesAndSucceeds() {
        Object mockProduct = new Object();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockProduct, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        ))
        .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
        .thenReturn(mockResponse);

        try {
            gatewayController.getPublicProduct("1");
        } catch (HttpServerErrorException e) {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }
    }

    @Test
    void processRequest_ServiceUnavailable_EventuallyFails() {
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        try {
            gatewayController.getPublicProduct("1");
        } catch (HttpServerErrorException e) {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
        }
    }

    @Test
    void testRecover() {
        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        ResponseEntity<?> response = gatewayController.recover(exception, "1");
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service Unavailable: please try again later after multiple attempts", response.getBody());
    }

    @Test
    void processRequest_UnexpectedException() {
        when(mockRestTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = gatewayController.getPublicProduct("1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody());
    }

    @Test
    void testTrailingSlashEndpoints() {
        Object mockProduct = new Object();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockProduct, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<?> responsePublic = gatewayController.getPublicProduct("1");
        assertEquals(HttpStatus.OK, responsePublic.getStatusCode());

        ResponseEntity<?> responseSecure = gatewayController.getSecureProduct("2");
        assertEquals(HttpStatus.OK, responseSecure.getStatusCode());
    }
}
