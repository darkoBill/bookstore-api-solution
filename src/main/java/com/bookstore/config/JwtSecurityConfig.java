package com.bookstore.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
@Profile("jwt")
public class JwtSecurityConfig {

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Value("${USER_USERNAME:user}")
    private String userUsername;

    @Value("${USER_PASSWORD:user123}")
    private String userPassword;

    @Value("${JWT_SIGNING_KEY:}")
    private String jwtSigningKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/metrics",
                    "/actuator/prometheus"
                ).permitAll()
                // Admin-only endpoints (POST, PUT, DELETE)
                .requestMatchers("POST", "/api/books").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/books/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/books/**").hasRole("ADMIN")
                .requestMatchers("/api/inventory/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // User and Admin can read books
                .requestMatchers("GET", "/api/books").hasAnyRole("USER", "ADMIN")
                .requestMatchers("GET", "/api/books/**").hasAnyRole("USER", "ADMIN")
                // Authenticated endpoints
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username(adminUsername)
            .password(passwordEncoder().encode(adminPassword))
            .roles("ADMIN")
            .build();

        UserDetails user = User.builder()
            .username(userUsername)  
            .password(passwordEncoder().encode(userPassword))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public KeyPair keyPair() {
        if (StringUtils.hasText(jwtSigningKey)) {
            // In production, use persistent key from environment
            // For demo purposes, we'll still generate but log a warning
            log.warn("JWT signing key provided via environment - using persistent key storage is recommended");
            // TODO: Implement proper key loading from environment/external store
        }
        
        try {
            // Generate RSA key pair
            // In production: load from secure key store, environment, or external service
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            log.warn("JWT keys generated at runtime - tokens will be invalid after restart. " +
                    "Use persistent key storage in production.");
            
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate RSA key pair", e);
        }
    }

    @Bean
    public RSAKey rsaKey(KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        return new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID("bookstore-jwt-key-" + UUID.randomUUID().toString())
            .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder
            .withPublicKey(rsaKey.toRSAPublicKey())
            .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}