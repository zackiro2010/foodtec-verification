package com.product.verification.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class FoodTecGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(FoodTecGatewayController.class);

    @Value("${foodtec.inventory.service.url:http://localhost:8081/internal/food-items/}")
    private String inventoryServiceUrl;

    @Value("${foodtec.inventory.service.key}")
    private String inventoryServiceKey;

    private final RestTemplate restTemplate;

    public FoodTecGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping({"/public/food-items/{id}", "/public/food-items/{id}/"})
    @Retryable(
            retryFor = {HttpServerErrorException.class},
            backoff = @Backoff(delay = 100)
    )
    public ResponseEntity<?> getPublicFoodItem(@PathVariable String id) {
        logger.info("Public request for food item id: {}", id);
        return processRequest(id);
    }

    @GetMapping({"/secure/food-items/{id}", "/secure/food-items/{id}/"})
    @Retryable(
            retryFor = {HttpServerErrorException.class},
            backoff = @Backoff(delay = 100)
    )
    public ResponseEntity<?> getSecureFoodItem(@PathVariable String id) {
        logger.info("Secure request for food item id: {}", id);
        return processRequest(id);
    }

    public ResponseEntity<?> processRequest(String id) {
        // Validate ID (must be numeric and positive)
        if (!id.matches("\\d+") || Integer.parseInt(id) <= 0) {
            logger.warn("Invalid food item ID in request: {}", id);
            return ResponseEntity.badRequest().body("Invalid food item ID. Must be a positive number.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Key", inventoryServiceKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.debug("Forwarding request to FoodTec Inventory Service for id: {}", id);
            ResponseEntity<Object> response = restTemplate.exchange(
                    inventoryServiceUrl + id,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            logger.warn("FoodTec Inventory Service returned error: {} for id: {}", e.getStatusCode(), id);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            logger.error("FoodTec Inventory Service error: {} for id: {}. Message: {}", e.getStatusCode(), id, e.getMessage());
            // Rethrow so @Retryable can pick it up
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while calling FoodTec Inventory Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @Recover
    public ResponseEntity<?> recover(HttpServerErrorException e, String id) {
        logger.error("Retry limit reached for food item id: {}. Final error: {}", id, e.getStatusCode());
        if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable: please try again later after multiple attempts");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error from downstream service after multiple attempts");
    }
}
