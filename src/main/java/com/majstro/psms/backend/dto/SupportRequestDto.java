package com.majstro.psms.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupportRequestDto(
        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = "^(Technical Issue|Account & Login|Projects|AI Assistant|Feature Request|Other)$",
                message = "Invalid category selected"
        )
        String category,

        @NotBlank(message = "Subject is required")
        @Size(min = 3, max = 150, message = "Subject must be between 3 and 150 characters")
        String subject,

        @NotBlank(message = "Message is required")
        @Size(min = 10, max = 3000, message = "Message must be between 10 and 3000 characters")
        String message,

        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 120, message = "Email must not exceed 120 characters")
        String email
) {}

