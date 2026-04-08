package com.union.app.auth.service.impl;

import com.union.app.auth.dto.AuthRequest;
import com.union.app.auth.dto.AuthResponse;
import com.union.app.auth.security.JwtTokenProvider;
import com.union.app.auth.service.AuthService;
import com.union.app.exception.auth.DuplicatePhoneNumberException;
import com.union.app.user.model.User;
import com.union.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider tokenProvider;

    @Override
    public void register(AuthRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException("Phone number already in use");
        }
        userRepository.existsByPhoneNumber(request.getPhoneNumber());
        User user = new User();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhoneNumber(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(request.getPhoneNumber());
        return new AuthResponse(token);
    }
}