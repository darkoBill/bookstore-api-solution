package com.bookstore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("basic")
public class SecurityConfig {
    
    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    @Value("${app.security.user.username}")
    private String userUsername;

    @Value("${app.security.user.password}")
    private String userPassword;

    @PostConstruct
    void validateCredentialProperties() {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)
            || !StringUtils.hasText(userUsername) || !StringUtils.hasText(userPassword)) {
            throw new IllegalStateException("Security credentials are not configured properly");
        }
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disabled for stateless REST API
            .csrf(csrf -> csrf.disable())
            // Stateless session for REST API scalability
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints for monitoring and documentation
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // All API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
            // Basic auth chosen for simplicity over JWT
            .httpBasic(withDefaults());
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // In-memory users for demo - production would use database or LDAP
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        
        manager.createUser(User.builder()
            .username(adminUsername)
            .password(passwordEncoder.encode(adminPassword))
            .roles("ADMIN")
            .build());
        
        manager.createUser(User.builder()
            .username(userUsername)
            .password(passwordEncoder.encode(userPassword))
            .roles("USER")
            .build());
        
        return manager;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}