package com.majstro.psms.backend.service;

import com.majstro.psms.backend.entity.Artifact;
import com.majstro.psms.backend.entity.ArtifactType;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.repository.ArtifactRepository;
import com.majstro.psms.backend.service.storage.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final FileStorageService storageService;

    public Artifact upload(MultipartFile file, Project project, ArtifactType type) {

        Artifact artifact = new Artifact();
        artifact.setOriginalFilename(file.getOriginalFilename());
        artifact.setContentType(file.getContentType());
        artifact.setSize(file.getSize());
        artifact.setType(type);
        artifact.setProject(project);
        artifact.setUploadedAt(LocalDateTime.now());

        artifact = artifactRepository.save(artifact);

        String path = storageService.store(file, project.getId(), artifact.getId());
        artifact.setStoragePath(path);

        return artifactRepository.save(artifact);
    }

    public Artifact getArtifactForProject(Long artifactId, String projectId) {
        return artifactRepository.findByIdAndProject_Id(artifactId, projectId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Artifact not found for this project"));
    }

    public Resource loadArtifactFile(Artifact artifact) {
        return storageService.load(artifact.getStoragePath());
    }
}