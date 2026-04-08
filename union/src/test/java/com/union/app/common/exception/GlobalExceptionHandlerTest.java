package com.union.app.common.exception;

import com.union.app.exception.AlreadyFriendsException;
import com.union.app.exception.UnionNotFoundException;
import com.union.app.exception.UserNotFoundException;
import com.union.app.exception.auth.DuplicatePhoneNumberException;
import com.union.app.exception.event.AccessNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindingResult;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesDuplicatePhoneNumber() {
        var response = handler.handleDuplicatePhoneNumber(new DuplicatePhoneNumberException("taken"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("taken", response.getBody().getMessage());
    }

    @Test
    void handlesAuthenticationException() {
        var response = handler.handleIncorrectAuth(new BadCredentialsException("bad creds"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Incorrect login or password", response.getBody().getError());
        assertEquals("bad creds", response.getBody().getMessage());
    }

    @Test
    void handlesAccessNotAllowed() {
        var response = handler.handleAccessNotAllowed(new AccessNotAllowedException("forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getError());
    }

    @Test
    void handlesUnionNotFound() {
        var response = handler.handleNotFoundUnion(new UnionNotFoundException("no union"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Union does not exist", response.getBody().getError());
    }

    @Test
    void handlesUserNotFound() {
        var response = handler.handleUserNotFound(new UserNotFoundException("no user"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    void handlesAlreadyFriends() {
        var response = handler.handleAlreadyFriends(new AlreadyFriendsException("already"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void handlesValidationErrors() throws Exception {
        Method method = Dummy.class.getDeclaredMethod("validMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Dummy(), "dummy");
        bindingResult.addError(new FieldError("dummy", "name", "required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("required", response.getBody().getValidationErrors().get("name"));
    }

    @Test
    void handlesGenericException() {
        var response = handler.handleGenericException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    static class Dummy {
        public void validMethod(String value) {
        }
    }
}
