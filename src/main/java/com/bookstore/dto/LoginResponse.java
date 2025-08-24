package com.bookstore.dto;

import java.time.Instant;
import java.util.List;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    Instant expiresAt,
    List<String> authorities,
    String username
) {}