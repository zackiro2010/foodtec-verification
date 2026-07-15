package com.product.verification.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FoodTecGatewayApplicationTest {

    @Test
    void testRestTemplateBean() {
        FoodTecGatewayApplication app = new FoodTecGatewayApplication();
        RestTemplate restTemplate = app.restTemplate();
        assertNotNull(restTemplate);
    }
}
