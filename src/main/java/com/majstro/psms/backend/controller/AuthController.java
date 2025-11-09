package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.service.IUserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserSyncService userSyncService;

    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            userSyncService.ensureUserExists(jwt);
        }
        System.out.println(" User sync endpoint called");
        return ResponseEntity.ok("User synced successfully");
    }
}