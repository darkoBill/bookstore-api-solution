package com.bookstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        log.info("*** JWT CONVERTER CALLED ***");
        log.info("Converting JWT to authentication token");
        log.info("JWT claims: {}", jwt.getClaims());
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        log.info("Extracted authorities: {}", authorities);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        log.debug("Extracting authorities from JWT");
        Collection<String> roles = null;
        
        // Debug: print all claims
        jwt.getClaims().forEach((k, v) -> log.debug("Claim '{}': {}", k, v));
        
        // Try as string list first
        if (jwt.hasClaim("roles")) {
            log.debug("Found 'roles' claim");
            try {
                roles = jwt.getClaimAsStringList("roles");
                log.debug("Successfully extracted roles as list: {}", roles);
            } catch (Exception e) {
                log.debug("Failed to extract as list: {}", e.getMessage());
                // If list fails, try as single string
                String roleString = jwt.getClaimAsString("roles");
                log.debug("Trying as string: {}", roleString);
                if (roleString != null && !roleString.trim().isEmpty()) {
                    roles = List.of(roleString.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                    log.debug("Parsed string roles: {}", roles);
                }
            }
        } else {
            log.debug("No 'roles' claim found in JWT");
        }
        
        if (roles != null && !roles.isEmpty()) {
            Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
            log.debug("Final authorities: {}", authorities);
            return authorities;
        }
        
        log.debug("No roles found, trying scope fallback");
        // Fallback to scope-based authorities
        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.trim().isEmpty()) {
            Collection<GrantedAuthority> scopeAuthorities = List.of(scope.split(" "))
                .stream()
                .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                .collect(Collectors.toList());
            log.debug("Scope-based authorities: {}", scopeAuthorities);
            return scopeAuthorities;
        }
        
        log.debug("No authorities found, returning empty list");
        // If no roles found, return empty list (this shouldn't happen in normal flow)
        return List.of();
    }
}