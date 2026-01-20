package com.majstro.psms.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload.base-path}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String projectId, Long artifactId) {
        try {
            Path dir = Paths.get(basePath, projectId.toString(), "artifacts");
            Files.createDirectories(dir);

            String storedName = artifactId + "_" + file.getOriginalFilename();
            Path target = dir.resolve(storedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public Resource load(String path) {
        try {
            return new UrlResource(Paths.get(path).toUri());
        } catch (Exception e) {
            throw new RuntimeException("File not found",e);
        }
    }
}