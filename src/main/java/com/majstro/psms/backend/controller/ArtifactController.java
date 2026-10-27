package com.majstro.psms.backend.controller;

import com.majstro.psms.backend.dto.ArtifactSummaryDto;
import com.majstro.psms.backend.dto.EmailDto;
import com.majstro.psms.backend.dto.GmailQueryDto;
import com.majstro.psms.backend.dto.ZoomTranscriptDto;
import com.majstro.psms.backend.entity.Artifact;
import com.majstro.psms.backend.entity.ArtifactType;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.mapper.ArtifactMapper;
import com.majstro.psms.backend.rag.RagServices;
import com.majstro.psms.backend.service.ArtifactService;
import com.majstro.psms.backend.service.IProjectService;
import com.majstro.psms.backend.service.IUserService;
import com.majstro.psms.backend.service.ThirdPartyServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final IUserService userService;
    private final ThirdPartyServices thirdPartyServices;

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
        var user = userService.getCurrentUser();

        ragServices.embbedAndStoreDocument(file, user.getId(), tags, projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ArtifactMapper.toUploadResponse(artifact));
    }

    @GetMapping("/{artifactId}/download")
    public ResponseEntity<java.util.Map<String, String>> download(
            @PathVariable String projectId,
            @PathVariable Long artifactId) {

        projectService.getProjectEntityById(projectId);
        Artifact artifact = artifactService.getArtifactForProject(artifactId, projectId);
        String signedUrl = artifactService.getDownloadUrl(artifact);

        return ResponseEntity.ok(java.util.Map.of("url", signedUrl));
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


    @PostMapping("/gmails")
    public ResponseEntity<List<EmailDto>> getGmails(@RequestBody GmailQueryDto queryDto) {
        var user = userService.getCurrentUser();
        String query = queryDto.buildQueryString();
        List<String> queries = List.of(query);
        List<EmailDto> emails = thirdPartyServices.getGmails(user.getId(), queries);
        return ResponseEntity.ok(emails);
    }


    @GetMapping("/zoom-transcripts/{meetingId}")
    public ResponseEntity<ZoomTranscriptDto> getZoomTranscripts(@PathVariable String meetingId) {
        var user = userService.getCurrentUser();
        String accessToken = user.getZoomAccessToken();
        var transcriptDto = thirdPartyServices.getZoomTranscripts(accessToken, meetingId);
        return ResponseEntity.ok(transcriptDto);
    }

}
