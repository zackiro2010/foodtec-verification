package com.product.verification.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/internal")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    @Value("${inventory.service.key}")
    private String validServiceKey;

    private final Map<String, Product> productData = new ConcurrentHashMap<>();
    private final Random random;
    
    public InventoryController() {
        this(new Random());
    }

    public InventoryController(Random random) {
        this.random = random;
        // Initialize in-memory product list
        productData.put("1", new Product("1", "Pizza", "Delicious cheese pizza", 12.99));
        productData.put("2", new Product("2", "Subs", "Fresh Italian sub", 8.50));
        productData.put("3", new Product("3", "Drinks", "Refreshing soft drink", 1.99));
        productData.put("4", new Product("4", "Pasta", "Creamy Alfredo pasta", 11.00));
        productData.put("5", new Product("5", "Salad", "Healthy Caesar salad", 7.25));
    }

    @GetMapping("/check/{id}")
    public ResponseEntity<?> checkProduct(
            @PathVariable String id,
            @RequestHeader(value = SERVICE_KEY_HEADER, required = false) String serviceKey) {

        logger.info("Received request for product id: {}", id);

        // Header Auth Validation
        if (serviceKey == null || !serviceKey.equals(validServiceKey)) {
            logger.warn("Unauthorized access attempt with key: {}", serviceKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Invalid Service Key");
        }

        // Validate ID (must be numeric and positive)
        if (!id.matches("\\d+") || Integer.parseInt(id) <= 0) {
            logger.warn("Invalid product ID: {}", id);
            return ResponseEntity.badRequest().body("Invalid product ID. Must be a positive number.");
        }

        // Simulate 10% Service Unavailable
        if (random.nextInt(100) < 10) {
            logger.error("Simulated Service Unavailable for product id: {}", id);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable: Try again later");
        }

        // Check if product exists
        Product product = productData.get(id);
        if (product == null) {
            logger.info("Product not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found with id: " + id);
        }

        logger.info("Product found: {}", product.name());
        return ResponseEntity.ok(product);
    }
}
