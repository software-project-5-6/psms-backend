package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.ArtifactSummaryDto;
import com.majstro.psms.backend.entity.Artifact;
import com.majstro.psms.backend.entity.ArtifactType;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.mapper.ArtifactMapper;
import com.majstro.psms.backend.rag.RagServices;
import com.majstro.psms.backend.service.ArtifactService;
import com.majstro.psms.backend.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private static final Logger log = LoggerFactory.getLogger(ArtifactController.class);

    private final ArtifactService artifactService;
    private final IProjectService projectService;
    private final RagServices ragServices;

    /**
     * Get all artifacts for a project
     */
    @GetMapping
    public ResponseEntity<List<ArtifactSummaryDto>> getArtifacts(@PathVariable String projectId) {
        projectService.getProjectEntityById(projectId); // Validate project exists
        List<Artifact> artifacts = artifactService.getArtifactsForProject(projectId);
        List<ArtifactSummaryDto> dtos = artifacts.stream()
                .map(ArtifactMapper::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Upload artifact
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @PathVariable String projectId,
            @RequestParam MultipartFile file,
            @RequestParam ArtifactType type,
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(required = false) String tags) {

        Project project = projectService.getProjectEntityById(projectId);
        Artifact artifact = artifactService.upload(file, project, type, uploadedBy, tags);

        ragServices.embbedAndStore(file,uploadedBy,tags,projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ArtifactMapper.toUploadResponse(artifact));
    }

    /**
     * Download artifact
     */
    @GetMapping("/{artifactId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String projectId,
            @PathVariable Long artifactId) {

        projectService.getProjectEntityById(projectId);
        Artifact artifact = artifactService.getArtifactForProject(artifactId, projectId);
        Resource resource = artifactService.loadArtifactFile(artifact);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + artifact.getOriginalFilename() + "\""
                )
                .body(resource);
    }

    /**
     * Delete artifact
     */
    @DeleteMapping("/{artifactId}")
    public ResponseEntity<Void> delete(
            @PathVariable String projectId,
            @PathVariable Long artifactId) {

        projectService.getProjectEntityById(projectId);
        artifactService.deleteArtifact(artifactId, projectId);
        ragServices.deleteDocs(projectId);
        return ResponseEntity.noContent().build();
    }
}
