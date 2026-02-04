package com.majstro.psms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ArtifactUploadResponseDto {

    private Long id;
    private String originalFilename;
    private String contentType;
    private long size;
    private String type;
    private LocalDateTime uploadedAt;
}