package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.SupportRequestDto;
import com.majstro.psms.backend.dto.SupportResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ISupportService {
    SupportResponseDto submitSupportRequest(SupportRequestDto request, Jwt jwt);
}

