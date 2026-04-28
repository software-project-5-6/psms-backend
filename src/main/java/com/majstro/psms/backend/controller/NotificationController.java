package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.DeviceTokenRequest;
import com.majstro.psms.backend.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    /**
     * Register device token for push notifications
     */
    @PostMapping("/register-device")
    public ResponseEntity<String> registerDevice(
            @Valid @RequestBody DeviceTokenRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName(); // This gets the cognitoSub from JWT
        
        log.info("Registering device for user: {} - Device Type: {}", userId, request.getDeviceType());
        
        pushNotificationService.registerDeviceToken(
                userId, 
                request.getToken(), 
                request.getDeviceType(), 
                request.getDeviceId()
        );
        
        return ResponseEntity.ok("Device registered successfully");
    }

    /**
     * Unregister device token
     */
    @DeleteMapping("/unregister-device")
    public ResponseEntity<String> unregisterDevice(
            @RequestParam String token,
            Authentication authentication) {
        
        log.info("Unregistering device token for user: {}", authentication.getName());
        
        pushNotificationService.unregisterDeviceToken(token);
        return ResponseEntity.ok("Device unregistered successfully");
    }
}
