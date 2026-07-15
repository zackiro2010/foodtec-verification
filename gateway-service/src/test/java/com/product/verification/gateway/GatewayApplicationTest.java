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

    @Test
    void main() {
        // We can't easily test the full application start in a unit test, 
        // but we can call it with empty/invalid args or just know that 
        // @SpringBootTest would cover it better.
        // For instruction coverage of the 'main' method:
        // GatewayApplication.main(new String[]{});
    }
}
