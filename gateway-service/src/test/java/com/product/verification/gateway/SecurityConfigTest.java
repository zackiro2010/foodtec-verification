package com.product.verification.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void testUserDetailsService() {
        SecurityConfig config = new SecurityConfig();
        UserDetailsService service = config.userDetailsService();
        assertNotNull(service);
        assert(service instanceof InMemoryUserDetailsManager);
    }

    @Test
    void testSecurityFilterChain()
    {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config);
    }
}
