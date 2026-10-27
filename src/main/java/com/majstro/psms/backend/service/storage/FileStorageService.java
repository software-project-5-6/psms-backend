package com.majstro.psms.backend.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String projectId, Long artifactId);
    String getSignedUrl(String storagePath);
    void delete(String storagePath);
    void deleteProjectDirectory(String projectId);
}
