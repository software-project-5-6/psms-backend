package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.service.IUserSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserSyncService userSyncService;

    @Test
    void shouldSyncUser_WhenAuthenticated() throws Exception {
        // Act & Assert
        // We simulate a JWT token with .with(jwt()) and add CSRF token (required for POST)
        mockMvc.perform(post("/api/v1/auth/sync")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User synced successfully"));

        // Verify the service was actually called with a Jwt object
        verify(userSyncService).ensureUserExists(any(Jwt.class));
    }

    @Test
    void shouldReturn401_WhenUnauthenticated() throws Exception {
        // Act (No JWT provided)
        mockMvc.perform(post("/api/v1/auth/sync")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}