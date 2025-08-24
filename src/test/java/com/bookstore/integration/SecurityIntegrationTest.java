package com.bookstore.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void accessProtectedEndpoint_WithValidAdminCredentials_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/books")
                .with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk());
    }
    
    @Test
    void accessProtectedEndpoint_WithValidUserCredentials_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/books")
                .with(httpBasic("user", "user123")))
            .andExpect(status().isOk());
    }
    
    @Test
    void accessProtectedEndpoint_WithInvalidCredentials_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/books")
                .with(httpBasic("admin", "wrongpassword")))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void accessProtectedEndpoint_WithoutCredentials_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void accessActuatorHealth_WithoutCredentials_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
    
    @Test
    void accessSwaggerUI_WithoutCredentials_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }
}