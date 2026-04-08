package com.union.app.auth.service;

import com.union.app.auth.dto.AuthRequest;
import com.union.app.auth.dto.AuthResponse;

public interface AuthService {
    void register(AuthRequest request);

    AuthResponse login(AuthRequest request);
}