package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.config.SecurityConfig;
import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HealthController.class, UserController.class})
@Import(SecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private IUserService userService;

    // --- Health Endpoint Tests ---

    @Test
    @DisplayName("GET /api/v1/health without token returns HTTP 200 OK with body 'OK'")
    void getHealth_WithoutToken_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("HEAD /api/v1/health without token returns HTTP 200 OK")
    void headHealth_WithoutToken_ReturnsOk() throws Exception {
        mockMvc.perform(head("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/health with token returns HTTP 200 OK with body 'OK'")
    void getHealth_WithToken_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/health").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("POST /api/v1/health without token returns HTTP 401 Unauthorized")
    void postHealth_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/health"))
                .andExpect(status().isUnauthorized());
    }

    // --- Existing Protected Endpoints Remain Protected ---

    @Test
    @DisplayName("GET /api/v1/users/me without token returns HTTP 401 Unauthorized")
    void getProtectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users without token returns HTTP 401 Unauthorized")
    void getAllUsers_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users/me with token returns HTTP 200 OK")
    void getProtectedEndpoint_WithToken_ReturnsOk() throws Exception {
        UserDto user = UserDto.builder()
                .id("1")
                .fullName("Test User")
                .email("test@example.com")
                .globalRole("APP_USER")
                .build();

        given(userService.getCurrentUser()).willReturn(user);

        mockMvc.perform(get("/api/v1/users/me").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users without APP_ADMIN authority returns HTTP 403 Forbidden")
    void getAdminEndpoint_WithoutAdminAuthority_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users with APP_ADMIN authority returns HTTP 200 OK")
    void getAdminEndpoint_WithAdminAuthority_ReturnsOk() throws Exception {
        UserDto u1 = UserDto.builder()
                .id("1")
                .fullName("Admin")
                .email("admin@test.com")
                .build();

        given(userService.getAllUsers()).willReturn(List.of(u1));

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_ADMIN"))))
                .andExpect(status().isOk());
    }
}
