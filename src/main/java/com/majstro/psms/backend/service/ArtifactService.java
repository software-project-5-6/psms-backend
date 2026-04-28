package com.majstro.psms.backend.service;

import com.majstro.psms.backend.entity.Artifact;
import com.majstro.psms.backend.entity.ArtifactType;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectUserRole;
import com.majstro.psms.backend.repository.ArtifactRepository;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.service.storage.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final FileStorageService storageService;
    private final ProjectRepository projectRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public Artifact upload(MultipartFile file, Project project, ArtifactType type, String uploadedBy, String tags) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // Generate unique stored filename
        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Artifact artifact = new Artifact();
        artifact.setOriginalFilename(file.getOriginalFilename());
        artifact.setStoredFilename(storedFilename);
        artifact.setContentType(file.getContentType());
        artifact.setSize(file.getSize());
        artifact.setType(type);
        artifact.setProject(project);
        artifact.setUploadedBy(uploadedBy);
        artifact.setTags(tags);
        
        // Set temporary storage path to satisfy NOT NULL constraint
        // Will be updated after we get the artifact ID
        artifact.setStoragePath("pending");

        // Save to get ID
        artifact = artifactRepository.save(artifact);

        // Now store the file with the actual ID and update path
        String path = storageService.store(file, project.getId(), artifact.getId());
        artifact.setStoragePath(path);

        // Update project artifact count
        project.setArtifactCount((project.getArtifactCount() == null ? 0 : project.getArtifactCount()) + 1);
        projectRepository.save(project);

        // Send push notifications to all project members (except uploader)
        sendArtifactUploadNotifications(project, artifact, uploadedBy);

        return artifactRepository.save(artifact);
    }

    public List<Artifact> getArtifactsForProject(String projectId) {
        return artifactRepository.findByProject_Id(projectId);
    }

    public Artifact getArtifactForProject(Long artifactId, String projectId) {
        return artifactRepository.findByIdAndProject_Id(artifactId, projectId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Artifact not found for this project"));
    }

    @Transactional
    public void deleteArtifact(Long artifactId, String projectId) {
        Artifact artifact = getArtifactForProject(artifactId, projectId);
        Project project = artifact.getProject();
        
        // Delete physical file
        try {
            storageService.delete(artifact.getStoragePath());
        } catch (Exception e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }
        artifactRepository.deleteByIdAndProject_Id(artifactId, projectId);
        
        // Update project artifact count
        if (project.getArtifactCount() != null && project.getArtifactCount() > 0) {
            project.setArtifactCount(project.getArtifactCount() - 1);
            projectRepository.save(project);
        }
    }

    public Resource loadArtifactFile(Artifact artifact) {
        return storageService.load(artifact.getStoragePath());
    }

    /**
     * Send push notifications to all project members when artifact is uploaded
     */
    private void sendArtifactUploadNotifications(Project project, Artifact artifact, String uploadedBy) {
        try {
            // Get all user IDs from the project (except the uploader)
            List<String> userIds = project.getUserRoles().stream()
                    .map(ProjectUserRole::getUser)
                    .map(user -> user.getId())
                    .filter(userId -> !userId.equals(uploadedBy))
                    .collect(Collectors.toList());

            if (userIds.isEmpty()) {
                log.debug("No users to notify for project: {}", project.getId());
                return;
            }

            // Prepare notification data
            Map<String, String> data = new HashMap<>();
            data.put("type", "ARTIFACT_UPLOADED");
            data.put("projectId", project.getId());
            data.put("projectName", project.getProjectName());
            data.put("artifactId", artifact.getId().toString());
            data.put("artifactName", artifact.getOriginalFilename());

            // Send notification
            String title = "New Artifact Uploaded";
            String body = String.format("A new artifact '%s' has been uploaded to project '%s'", 
                                       artifact.getOriginalFilename(), 
                                       project.getProjectName());

            pushNotificationService.sendNotificationToMultipleUsers(userIds, title, body, data);
            
            log.info("Sent artifact upload notifications to {} users in project {}", 
                    userIds.size(), project.getId());
            
        } catch (Exception e) {
            log.error("Failed to send artifact upload notifications for project {}: {}", 
                     project.getId(), e.getMessage(), e);
            // Don't throw - notification failure shouldn't stop the artifact upload
        }
    }
}