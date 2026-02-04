package com.majstro.psms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ArtifactSummaryDto {

    private Long id;
    private String originalFilename;
    private String type;
    private long size;
    private String uploadedBy;
    private String tags;
    private LocalDateTime uploadedAt;
}