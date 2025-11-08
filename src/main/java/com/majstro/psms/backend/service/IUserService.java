package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface IUserService {

    // Get all users (for admin views)
    List<UserDto> getAllUsers();

    // Get a single user by ID
    UserDto getUserById(String id);

    // Get a user by Cognito sub
    UserDto getUserByCognitoSub(String cognitoSub);

    // Get currently logged-in user (based on JWT)
    UserDto getCurrentUser();

    // Delete a user by ID (optional, admin only)
    void deleteUser(String id);
}