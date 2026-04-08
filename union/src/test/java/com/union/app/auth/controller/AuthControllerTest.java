package com.union.app.auth.controller;

import com.union.app.auth.dto.AuthRequest;
import com.union.app.auth.dto.AuthResponse;
import com.union.app.auth.service.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void registersAndLogsIn() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        AuthRequest request = new AuthRequest();
        request.setPhoneNumber("79990001122");
        request.setPassword("secret");
        when(authService.login(request)).thenReturn(new AuthResponse("token"));

        var registerResponse = controller.register(request);
        var loginResponse = controller.login(request);

        assertEquals("success", registerResponse.getBody().get("status"));
        assertEquals("79990001122", registerResponse.getBody().get("phoneNumber"));
        assertEquals("token", loginResponse.getBody().getToken());
        verify(authService).register(request);
        verify(authService).login(request);
    }
}
