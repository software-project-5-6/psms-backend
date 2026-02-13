package com.majstro.psms.backend.rag.util;

import com.majstro.psms.backend.rag.dataModel.RAGDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class RagUtil {

    public RAGDocument convertToRagDocument(
            MultipartFile file,
            String uploadedBy,
            String tags,
            String projectId) throws IOException {


        if (file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("File is empty and cannot be processed for embeddings");
        }


        TikaDocumentReader reader =
                new TikaDocumentReader(new InputStreamResource(file.getInputStream()));

        List<Document> docs = reader.read();


        if (docs.isEmpty()) {
            throw new IllegalArgumentException("No content could be extracted from the file");
        }


        String content = docs.stream()
                .map(Document::getFormattedContent)
                .reduce("", (a, b) -> a + "\n" + b);


        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("Extracted content is empty and cannot be embedded");
        }


        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectId", projectId);
        metadata.put("fileName", file.getOriginalFilename());
        metadata.put("uploadedBy", uploadedBy != null ? uploadedBy : "unknown");
        metadata.put("tags", tags != null ? tags : "");
        metadata.put("size", file.getSize());
        metadata.put("type", file.getContentType());


        return new RAGDocument(
                UUID.randomUUID().toString(),
                content,
                metadata);
    }
}
