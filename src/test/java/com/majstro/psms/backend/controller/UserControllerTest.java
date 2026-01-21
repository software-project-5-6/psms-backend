package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.config.SecurityConfig; // <--- 1. Import your config
import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // <--- 2. Import this annotation
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // <--- 3. ADD THIS LINE (Activates @PreAuthorize)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Test
    void shouldGetCurrentUser_WhenAuthenticated() throws Exception {
        UserDto user = UserDto.builder()
                .id("1")
                .fullName("Me")
                .email("me@test.com")
                .globalRole("USER")
                .build();

        given(userService.getCurrentUser()).willReturn(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test
    void shouldGetAllUsers_WhenAdmin() throws Exception {
        UserDto u1 = UserDto.builder()
                .id("1")
                .fullName("Admin User")
                .email("admin@test.com")
                .build();

        given(userService.getAllUsers()).willReturn(List.of(u1));

        // The SecurityConfig is now active, so this AUTHORITY is required
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void shouldForbidGetAllUsers_WhenNotAdmin() throws Exception {
        // Now that SecurityConfig is imported, @PreAuthorize will fire
        // and reject this user because they lack 'APP_ADMIN'
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt()))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void shouldGetUserById_WhenAdmin() throws Exception {
        UserDto user = UserDto.builder()
                .id("99")
                .fullName("Target User")
                .build();

        given(userService.getUserById("99")).willReturn(user);

        mockMvc.perform(get("/api/v1/users/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUser_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/users/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_ADMIN")))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("123");
    }
}