package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.SupportRequestDto;
import com.majstro.psms.backend.dto.SupportResponseDto;
import com.majstro.psms.backend.service.ISupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final ISupportService supportService;

    @PostMapping("/requests")
    public ResponseEntity<SupportResponseDto> createSupportRequest(
            @Valid @RequestBody SupportRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        SupportResponseDto response = supportService.submitSupportRequest(request, jwt);
        return ResponseEntity.ok(response);
    }
}

