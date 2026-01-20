package com.majstro.psms.backend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String projectId, Long artifactId);
    Resource load(String path);
    void delete(String path);
}
