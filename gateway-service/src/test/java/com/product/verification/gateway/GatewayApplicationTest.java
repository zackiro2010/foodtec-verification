package com.product.verification.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayApplicationTest {

    @Test
    void testRestTemplateBean() {
        GatewayApplication app = new GatewayApplication();
        RestTemplate restTemplate = app.restTemplate();
        assertNotNull(restTemplate);
    }
}
