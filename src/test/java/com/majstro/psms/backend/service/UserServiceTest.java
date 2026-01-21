package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.mapper.UserMapper;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldGetUserById() {
        // Arrange
        String userId = "123";
        User user = new User();
        user.setId(userId);
        UserDto userDto = UserDto.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        UserDto result = userService.getUserById(userId);

        // Assert
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        // Arrange
        String userId = "999";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                userService.getUserById(userId)
        );
    }

    @Test
    void shouldGetUserIdFromJwt() {
        // Arrange
        String cognitoSub = "sub-123";
        String internalId = "db-id-123";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn(cognitoSub);

        User user = new User();
        user.setId(internalId);

        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(user));

        // Act
        String resultId = userService.getUserIdFromJwt(jwt);

        // Assert
        assertThat(resultId).isEqualTo(internalId);
    }
}