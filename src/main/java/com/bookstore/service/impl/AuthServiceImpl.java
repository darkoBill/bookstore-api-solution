package com.bookstore.service.impl;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("jwt")
public class AuthServiceImpl implements AuthService {
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    
    private static final long JWT_EXPIRATION_SECONDS = 3600; // 1 hour
    
    @Override
    public LoginResponse login(LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(JWT_EXPIRATION_SECONDS);
        
        List<String> roles = user.getAuthorities().stream()
            .map(authority -> authority.getAuthority().replace("ROLE_", ""))
            .toList();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("bookstore-api")
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(user.getUsername())
            .claim("roles", roles)
            .build();
        
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        
        log.info("Generated JWT token for user: {}", request.username());
        
        return new LoginResponse(
            token,
            "Bearer",
            JWT_EXPIRATION_SECONDS,
            expiry,
            roles,
            user.getUsername()
        );
    }
}