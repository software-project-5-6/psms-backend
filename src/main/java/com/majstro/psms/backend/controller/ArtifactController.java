package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.entity.Artifact;
import com.majstro.psms.backend.entity.ArtifactType;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.mapper.ArtifactMapper;
import com.majstro.psms.backend.service.ArtifactService;
import com.majstro.psms.backend.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects/{projectId}/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;
    private final IProjectService projectService;


    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @PathVariable String projectId,
            @RequestParam MultipartFile file,
            @RequestParam ArtifactType type) {

        Project project = projectService.getProjectEntityById(projectId); // reuse existing logic
        Artifact artifact = artifactService.upload(file, project, type);
        return ResponseEntity.ok(ArtifactMapper.toUploadResponse(artifact));
    }

    @GetMapping("/{artifactId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String projectId,
            @PathVariable Long artifactId) {

        // 1) validate project exists (reuse your service)
        projectService.getProjectEntityById(projectId);

        // 2) fetch artifact safely (project-bound)
        Artifact artifact = artifactService.getArtifactForProject(artifactId, projectId);

        // 3) load file
        Resource resource = artifactService.loadArtifactFile(artifact);

        // 4) stream response
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + artifact.getOriginalFilename() + "\""
                )
                .body(resource);
    }
}
