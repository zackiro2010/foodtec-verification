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
public class FoodTecInventoryController {

    private static final Logger logger = LoggerFactory.getLogger(FoodTecInventoryController.class);
    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    @Value("${foodtec.inventory.service.key}")
    private String validServiceKey;

    private final Map<String, FoodItem> productData = new ConcurrentHashMap<>();
    private final Random random;
    
    public FoodTecInventoryController() {
        this(new Random());
    }

    public FoodTecInventoryController(Random random) {
        this.random = random;
        // Initialize in-memory food item list
        productData.put("1", new FoodItem("1", "Pizza", "Delicious cheese pizza", 12.99));
        productData.put("2", new FoodItem("2", "Subs", "Fresh Italian sub", 8.50));
        productData.put("3", new FoodItem("3", "Drinks", "Refreshing soft drink", 1.99));
        productData.put("4", new FoodItem("4", "Pasta", "Creamy Alfredo pasta", 11.00));
        productData.put("5", new FoodItem("5", "Salad", "Healthy Caesar salad", 7.25));
    }

    @GetMapping("/food-items/{id}")
    public ResponseEntity<?> checkFoodItem(
            @PathVariable String id,
            @RequestHeader(value = SERVICE_KEY_HEADER, required = false) String serviceKey) {

        logger.info("Received request for food item id: {}", id);

        // Header Auth Validation
        if (serviceKey == null || !serviceKey.equals(validServiceKey)) {
            logger.warn("Unauthorized access attempt with key: {}", serviceKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Invalid Service Key");
        }

        // Validate ID (must be numeric and positive)
        if (!id.matches("\\d+") || Integer.parseInt(id) <= 0) {
            logger.warn("Invalid food item ID: {}", id);
            return ResponseEntity.badRequest().body("Invalid food item ID. Must be a positive number.");
        }

        // Simulate 10% Service Unavailable
        if (random.nextInt(100) < 10) {
            logger.error("Simulated Service Unavailable for food item id: {}", id);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable: Try again later");
        }

        // Check if food item exists
        FoodItem foodItem = productData.get(id);
        if (foodItem == null) {
            logger.info("Food item not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Food item not found with id: " + id);
        }

        logger.info("Food item found: {}", foodItem.name());
        return ResponseEntity.ok(foodItem);
    }
}
