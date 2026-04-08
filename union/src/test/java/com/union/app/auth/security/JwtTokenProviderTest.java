package com.union.app.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "mySecretKeyForJWTTokenGenerationWithEnoughLength32chars";

    @Test
    void generatesAndValidatesToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.generateToken("79990001122");

        assertAll(
                () -> assertNotNull(token),
                () -> assertTrue(provider.validateToken(token)),
                () -> assertEquals("79990001122", provider.getPhoneNumberFromToken(token))
        );
    }

    @Test
    void rejectsMalformedToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        assertFalse(provider.validateToken("not-a-jwt"));
    }
}
