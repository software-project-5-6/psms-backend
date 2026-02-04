package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.ArtifactSummaryDto;
import com.majstro.psms.backend.dto.ArtifactUploadResponseDto;
import com.majstro.psms.backend.entity.Artifact;

public class ArtifactMapper {

    public static ArtifactUploadResponseDto toUploadResponse(Artifact artifact) {
        return ArtifactUploadResponseDto.builder()
                .id(artifact.getId())
                .originalFilename(artifact.getOriginalFilename())
                .contentType(artifact.getContentType())
                .size(artifact.getSize())
                .type(artifact.getType().name())
                .uploadedAt(artifact.getUploadedAt())
                .build();
    }

    public static ArtifactSummaryDto toSummary(Artifact artifact) {
        return ArtifactSummaryDto.builder()
                .id(artifact.getId())
                .originalFilename(artifact.getOriginalFilename())
                .type(artifact.getType().name())
                .size(artifact.getSize())
                .uploadedBy(artifact.getUploadedBy())
                .tags(artifact.getTags())
                .uploadedAt(artifact.getUploadedAt())
                .build();
    }
}
