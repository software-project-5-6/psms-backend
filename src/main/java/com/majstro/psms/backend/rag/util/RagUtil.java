package com.majstro.psms.backend.rag.util;

import com.majstro.psms.backend.rag.model.RAGDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class RagUtil {

    public RAGDocument convertToRagDocument(
            MultipartFile file,
            String uploadedBy,
            String tags) throws IOException {

        // Create reader from InputStream
        TikaDocumentReader reader =
                new TikaDocumentReader((Resource) file.getInputStream());

        List<Document> docs = reader.read();

        // Combine all text
        String content = docs.stream()
                .map(Document::getFormattedContent)
                .reduce("", (a, b) -> a + "\n" + b);

        // metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", file.getOriginalFilename());
        metadata.put("uploadedBy", uploadedBy != null ? uploadedBy : "unknown");
        metadata.put("tags", tags != null ? tags : "");
        metadata.put("size", file.getSize());
        metadata.put("type", file.getContentType());

        return new RAGDocument(
                UUID.randomUUID().toString(),
                content,
                metadata
        );
    }
}
