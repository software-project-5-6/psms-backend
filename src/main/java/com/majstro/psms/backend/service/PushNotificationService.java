package com.majstro.psms.backend.service;

import com.google.firebase.messaging.*;
import com.majstro.psms.backend.entity.DeviceToken;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.repository.DeviceTokenRepository;
import com.majstro.psms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    /**
     * Register device token for a user
     * @param cognitoSub The Cognito subject ID from JWT
     */
    @Transactional
    public void registerDeviceToken(String cognitoSub, String token, String deviceType, String deviceId) {
        User user = userRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<DeviceToken> existing = deviceTokenRepository.findByTokenAndActiveTrue(token);
        
        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.setUser(user);
            deviceToken.setDeviceType(deviceType);
            deviceToken.setDeviceId(deviceId);
            deviceTokenRepository.save(deviceToken);
            log.info("Updated existing device token for user: {}", user.getId());
        } else {
            DeviceToken newToken = DeviceToken.builder()
                    .user(user)
                    .token(token)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .active(true)
                    .build();
            deviceTokenRepository.save(newToken);
            log.info("Registered new device token for user: {}", user.getId());
        }
    }

    /**
     * Unregister device token
     */
    @Transactional
    public void unregisterDeviceToken(String token) {
        deviceTokenRepository.findByTokenAndActiveTrue(token)
                .ifPresent(deviceToken -> {
                    deviceToken.setActive(false);
                    deviceTokenRepository.save(deviceToken);
                    log.info("Device token unregistered: {}", token);
                });
    }

    /**
     * Send notification to a specific user
     */
    public void sendNotificationToUser(String userId, String title, String body, 
                                       Map<String, String> data) {
        if (!firebaseEnabled) {
            log.debug("Firebase disabled. Skipping notification to user: {}", userId);
            return;
        }

        // Get all active device tokens for user
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUser_IdAndActiveTrue(userId);
        
        if (deviceTokens.isEmpty()) {
            log.debug("No active device tokens found for user: {}", userId);
            return;
        }

        // Send to each device
        for (DeviceToken deviceToken : deviceTokens) {
            sendToDevice(deviceToken, title, body, data);
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendNotificationToMultipleUsers(List<String> userIds, String title, 
                                                String body, Map<String, String> data) {
        if (!firebaseEnabled) {
            log.debug("Firebase disabled. Skipping notifications");
            return;
        }

        for (String userId : userIds) {
            sendNotificationToUser(userId, title, body, data);
        }
    }

    /**
     * Send notification to device
     */
    private void sendToDevice(DeviceToken deviceToken, String title, String body, 
                             Map<String, String> data) {
        try {
            Map<String, String> notificationData = data != null ? new HashMap<>(data) : new HashMap<>();
            
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(notificationData)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setColor("#4CAF50")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setBadge(1)
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent notification - Response: {}", response);
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to device: {} - Error: {}", 
                     deviceToken.getToken(), e.getMessage());
            
            // If token is invalid, mark it as inactive
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                deviceToken.setActive(false);
                deviceTokenRepository.save(deviceToken);
                log.info("Marked invalid device token as inactive");
            }
        } catch (Exception e) {
            log.error("Unexpected error sending notification: {}", e.getMessage(), e);
        }
    }
}
