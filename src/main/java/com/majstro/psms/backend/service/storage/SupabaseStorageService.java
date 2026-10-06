package com.majstro.psms.backend.service.storage;

import com.majstro.psms.backend.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseStorageService implements FileStorageService {

    private final String supabaseUrl;
    private final String bucket;
    private final RestClient restClient;

    public SupabaseStorageService(
            RestClient.Builder builder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service.key}") String serviceKey,
            @Value("${supabase.storage.bucket}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.restClient = builder
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .build();
    }

    @Override
    public String store(MultipartFile file, String projectId, Long artifactId) {
        String objectPath = projectId + "/artifacts/" + artifactId + "_" + file.getOriginalFilename();
        try {
            // Use an absolute URI so RestClient doesn't combine it with the base URL
            // (a relative path starting with '/' would drop '/storage/v1' from the base).
            URI uri = URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(objectPath));
            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.parseMediaType(
                            file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                    .header("x-upsert", "true")
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to Supabase Storage", e);
        }
        return objectPath;
    }

    @Override
    public String getSignedUrl(String storagePath) {
        try {
            URI uri = URI.create(supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + encodePath(storagePath));
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("signedURL")) {
                throw new FileStorageException("Supabase did not return a signed URL", null);
            }
            // Supabase returns signedURL as a path relative to /storage/v1 (e.g. "/object/sign/...").
            // We must prepend both supabaseUrl and "/storage/v1" to get a valid absolute URL.
            String signedPath = (String) response.get("signedURL");
            String base = signedPath.startsWith("/storage/v1") ? supabaseUrl : supabaseUrl + "/storage/v1";
            return base + signedPath;
        } catch (FileStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate signed URL", e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri("/object/{bucket}", bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", List.of(storagePath)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from Supabase Storage", e);
        }
    }

    @Override
    public void deleteProjectDirectory(String projectId) {
        List<String> paths = listObjectPaths(projectId + "/");
        if (paths.isEmpty()) return;

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri("/object/{bucket}", bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", paths))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete project directory from Supabase Storage", e);
        }
    }

    /** Encodes each path segment individually so '/' separators are preserved. */
    private String encodePath(String path) {
        String[] segments = path.split("/", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(UriUtils.encodePathSegment(segments[i], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> listObjectPaths(String prefix) {
        try {
            List<Map<String, Object>> items = restClient.post()
                    .uri("/object/list/{bucket}", bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefix", prefix, "limit", 1000, "offset", 0))
                    .retrieve()
                    .body(List.class);

            if (items == null) return List.of();
            return items.stream()
                    .map(item -> prefix + item.get("name"))
                    .toList();
        } catch (Exception e) {
            throw new FileStorageException("Failed to list objects in Supabase Storage", e);
        }
    }
}
