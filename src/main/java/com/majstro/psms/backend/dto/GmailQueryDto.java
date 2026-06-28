package com.majstro.psms.backend.dto;

import lombok.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Simplified DTO for querying Gmail API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmailQueryDto {


    private String senderEmail;
    private Integer lastDays;
    private String label;

    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer maxResults = 20;

    public String buildQueryString() {
        StringBuilder query = new StringBuilder();

        if (senderEmail != null && !senderEmail.isEmpty()) {
            query.append("from:").append(senderEmail).append(" ");
        }
        if (lastDays != null && lastDays > 0) {
            query.append("newer_than:").append(lastDays).append("d ");
        }
        if (label != null && !label.isEmpty()) {
            query.append("label:").append(label).append(" ");
        }

        return query.toString().trim();
    }
}

