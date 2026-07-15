package com.product.verification.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Value("${inventory.service.url:http://localhost:8081/internal/check/}")
    private String inventoryServiceUrl;

    @Value("${inventory.service.key:internal-secret-key}")
    private String inventoryServiceKey;

    private final RestTemplate restTemplate;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping({"/public/products/{id}", "/public/products/{id}/"})
    public ResponseEntity<?> getPublicProduct(@PathVariable String id) {
        logger.info("Public request for product id: {}", id);
        return processRequest(id);
    }

    @GetMapping({"/secure/products/{id}", "/secure/products/{id}/"})
    public ResponseEntity<?> getSecureProduct(@PathVariable String id) {
        logger.info("Secure request for product id: {}", id);
        return processRequest(id);
    }

    private ResponseEntity<?> processRequest(String id) {
        // Validate ID (must be numeric and positive)
        if (!id.matches("\\d+") || Integer.parseInt(id) <= 0) {
            logger.warn("Invalid product ID in request: {}", id);
            return ResponseEntity.badRequest().body("Invalid product ID. Must be a positive number.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Key", inventoryServiceKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.debug("Forwarding request to Inventory Service for id: {}", id);
            ResponseEntity<Object> response = restTemplate.exchange(
                    inventoryServiceUrl + id,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            logger.warn("Inventory Service returned error: {} for id: {}", e.getStatusCode(), id);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            logger.error("Inventory Service error: {} for id: {}", e.getStatusCode(), id);
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable: please try again later");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error from downstream service");
        } catch (Exception e) {
            logger.error("Unexpected error while calling Inventory Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
