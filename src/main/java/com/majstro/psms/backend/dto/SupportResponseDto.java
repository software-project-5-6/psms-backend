package com.majstro.psms.backend.dto;

import java.time.LocalDateTime;

public record SupportResponseDto(
        boolean success,
        String message,
        String ticketId,
        LocalDateTime timestamp
) {}

