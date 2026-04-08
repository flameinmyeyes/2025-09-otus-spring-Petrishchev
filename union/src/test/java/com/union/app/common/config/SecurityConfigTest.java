package com.union.app.common.config;

import com.union.app.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void corsConfigurationContainsExpectedOriginsAndMethods() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = ((UrlBasedCorsConfigurationSource) source).getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("GET", "/"));

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:3002"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
        assertTrue(cors.getAllowCredentials());
    }

    @Test
    void passwordEncoderWorks() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        var encoder = config.passwordEncoder();

        assertTrue(encoder.matches("secret", encoder.encode("secret")));
    }

    @Test
    void authenticationManagerDelegatesToConfiguration() throws Exception {
        AuthenticationConfiguration authConfiguration = mock(AuthenticationConfiguration.class);
        org.springframework.security.authentication.AuthenticationManager expected = mock(org.springframework.security.authentication.AuthenticationManager.class);
        when(authConfiguration.getAuthenticationManager()).thenReturn(expected);

        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));
        var actual = config.authenticationManager(authConfiguration);

        assertSame(expected, actual);
    }
}
