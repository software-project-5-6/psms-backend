package com.majstro.psms.backend.service;

import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.impl.UserSyncServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSyncServiceImpl userSyncService;

    @Test
    void shouldCreateUser_WhenNotExists() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn("cognito-123");
        when(jwt.getClaimAsString("email")).thenReturn("new@test.com");
        when(jwt.getClaimAsString("name")).thenReturn("New User");

        // Simulate user NOT found in DB
        when(userRepository.existsByCognitoSub("cognito-123")).thenReturn(false);

        // Act
        userSyncService.ensureUserExists(jwt);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDoNothing_WhenUserAlreadyExists() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn("cognito-123");

        // Simulate user IS found
        when(userRepository.existsByCognitoSub("cognito-123")).thenReturn(true);

        // Act
        userSyncService.ensureUserExists(jwt);

        // Assert
        verify(userRepository, never()).save(any());
    }
}