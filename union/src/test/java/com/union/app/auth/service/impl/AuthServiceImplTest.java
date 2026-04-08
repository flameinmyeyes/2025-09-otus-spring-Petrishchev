package com.union.app.auth.service.impl;

import com.union.app.auth.dto.AuthRequest;
import com.union.app.auth.security.JwtTokenProvider;
import com.union.app.exception.auth.DuplicatePhoneNumberException;
import com.union.app.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerSavesEncodedUser() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        when(userRepository.existsByPhoneNumber("79990001122")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        AuthServiceImpl service = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, tokenProvider);
        AuthRequest request = new AuthRequest();
        request.setPhoneNumber("79990001122");
        request.setPassword("plain");

        service.register(request);

        ArgumentCaptor<com.union.app.user.model.User> captor = ArgumentCaptor.forClass(com.union.app.user.model.User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("79990001122", captor.getValue().getPhoneNumber());
        assertEquals("encoded", captor.getValue().getPassword());
    }

    @Test
    void registerThrowsWhenPhoneAlreadyExists() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        when(userRepository.existsByPhoneNumber("79990001122")).thenReturn(true);

        AuthServiceImpl service = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, tokenProvider);
        AuthRequest request = new AuthRequest();
        request.setPhoneNumber("79990001122");
        request.setPassword("plain");

        assertThrows(DuplicatePhoneNumberException.class, () -> service.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginAuthenticatesAndReturnsToken() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken("79990001122")).thenReturn("jwt-token");

        AuthServiceImpl service = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, tokenProvider);
        AuthRequest request = new AuthRequest();
        request.setPhoneNumber("79990001122");
        request.setPassword("plain");

        var response = service.login(request);

        assertEquals("jwt-token", response.getToken());
        assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken("79990001122");
    }
}
