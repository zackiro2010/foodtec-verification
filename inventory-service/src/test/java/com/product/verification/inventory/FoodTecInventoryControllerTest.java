package com.product.verification.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class FoodTecInventoryControllerTest {

    private FoodTecInventoryController inventoryController;
    private Random mockRandom;
    private static final String VALID_KEY = "internal-secret-key";

    @BeforeEach
    void setUp() {
        mockRandom = Mockito.mock(Random.class);
        // By default, simulate no service failure (random >= 10)
        when(mockRandom.nextInt(anyInt())).thenReturn(50);
        inventoryController = new FoodTecInventoryController(mockRandom);
        org.springframework.test.util.ReflectionTestUtils.setField(inventoryController, "validServiceKey", VALID_KEY);
    }

    @Test
    void checkFoodItem_Success() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("1", VALID_KEY);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object body = response.getBody();
        assert body instanceof FoodItem;
        FoodItem foodItem = (FoodItem) body;
        assertEquals("Pizza", foodItem.name());
    }

    @Test
    void checkFoodItem_Unauthorized_MissingKey() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("1", null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden: Invalid Service Key", response.getBody());
    }

    @Test
    void checkFoodItem_Unauthorized_InvalidKey() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("1", "wrong-key");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void checkFoodItem_InvalidId_NonNumeric() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("abc", VALID_KEY);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void checkFoodItem_InvalidId_Negative() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("-1", VALID_KEY);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void checkFoodItem_NotFound() {
        ResponseEntity<?> response = inventoryController.checkFoodItem("99", VALID_KEY);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Food item not found with id: 99", response.getBody());
    }

    @Test
    void checkFoodItem_ServiceUnavailable() {
        // Simulate 10% failure (random < 10)
        when(mockRandom.nextInt(100)).thenReturn(5);
        ResponseEntity<?> response = inventoryController.checkFoodItem("1", VALID_KEY);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service Unavailable: Try again later", response.getBody());
    }
}
