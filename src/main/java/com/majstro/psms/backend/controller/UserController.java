package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    //Get currently logged-in user (available for all authenticated users)
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    //Get all users (ADMIN only)
    @PreAuthorize("hasAuthority('APP_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //Get user by ID (ADMIN only)
    @PreAuthorize("hasAuthority('APP_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //Get user by Cognito Sub (ADMIN only)
    @PreAuthorize("hasAuthority('APP_ADMIN')")
    @GetMapping("/sub/{cognitoSub}")
    public ResponseEntity<UserDto> getUserByCognitoSub(@PathVariable String cognitoSub) {
        return ResponseEntity.ok(userService.getUserByCognitoSub(cognitoSub));
    }

    //Delete a user by ID (ADMIN only)
    @PreAuthorize("hasAuthority('APP_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}