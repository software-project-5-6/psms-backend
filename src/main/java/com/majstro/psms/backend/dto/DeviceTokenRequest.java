package com.majstro.psms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "Device type is required")
    private String deviceType; // "ANDROID" or "IOS"
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
}
