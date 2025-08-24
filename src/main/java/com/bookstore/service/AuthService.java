package com.bookstore.service;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}