package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.mapper.UserMapper;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getUserByCognitoSub(String cognitoSub) {
        User user = userRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new EntityNotFoundException("User not found with Cognito Sub: " + cognitoSub));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated user found");
        }

        String sub = jwt.getClaimAsString("sub");

        User user = userRepository.findByCognitoSub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found in database for sub: " + sub));

        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}