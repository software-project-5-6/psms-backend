package com.majstro.psms.backend.rag.util;

import com.majstro.psms.backend.rag.dataModel.MetaData;
import com.majstro.psms.backend.rag.dataModel.VectorDataBlock;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class RagUtil {

    public VectorDataBlock convertDocumentToVectorDataBlock(
            MultipartFile file,
            String userId,
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

        MetaData metadata = new MetaData();
        metadata.type = "knowledge";
        metadata.subType = tags != null ? tags : "";
        metadata.userId = userId;
        metadata.projectId = projectId;


        return new VectorDataBlock(
                UUID.randomUUID().toString(),
                content,
                metadata);
    }

    public VectorDataBlock convertChatToVectorDataBlock(
            String message,
            String role,        // e.g. "user" or "assistant"
            String userId,
            String projectId,
            String conversationId) {

        MetaData metadata = new MetaData();
        metadata.type = "chat";
        metadata.subType = role;
        metadata.userId = userId;
        metadata.projectId = projectId;
        metadata.conversationId = conversationId;

        return new VectorDataBlock(
                UUID.randomUUID().toString(),
                message,
                metadata);
    }
}
