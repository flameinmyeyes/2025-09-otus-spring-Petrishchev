package com.union.app.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void ofWithoutPathBuildsBasicResponse() {
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND, "missing");

        assertAll(
                () -> assertEquals(404, response.getStatus()),
                () -> assertEquals("Not Found", response.getError()),
                () -> assertEquals("missing", response.getMessage()),
                () -> assertNull(response.getPath())
        );
    }

    @Test
    void ofWithPathBuildsBasicResponse() {
        ErrorResponse response = ErrorResponse.of(HttpStatus.FORBIDDEN, "denied", "/api/test");

        assertAll(
                () -> assertEquals(403, response.getStatus()),
                () -> assertEquals("Forbidden", response.getError()),
                () -> assertEquals("denied", response.getMessage()),
                () -> assertEquals("/api/test", response.getPath())
        );
    }

    @Test
    void validationErrorBuildsValidationPayload() {
        ErrorResponse response = ErrorResponse.validationError(Map.of("field", "must not be blank"));

        assertAll(
                () -> assertEquals(400, response.getStatus()),
                () -> assertEquals("Validation Failed", response.getError()),
                () -> assertEquals("Invalid request parameters", response.getMessage()),
                () -> assertEquals("must not be blank", response.getValidationErrors().get("field"))
        );
    }
}
