package com.majstro.psms.backend.rag.ingestion;


import com.majstro.psms.backend.rag.dataModel.VectorDataBlock;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {


    @Autowired
    VectorStore vectorStore;

    @Autowired
    TokenTextSplitter splitter;


    public void indexRagDocument(VectorDataBlock ragDoc) {
        try {

            if (ragDoc == null) {
                throw new IllegalArgumentException("RAGDocument cannot be null");
            }
            if (ragDoc.getContent() == null || ragDoc.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("RAGDocument content is empty");
            }

            Document doc = new Document(ragDoc.getContent(), ragDoc.getMetadata());

            List<Document> chunks = splitter.split(doc);
            vectorStore.add(chunks);        // embeddings + storage handled automatically

        } catch (Exception e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }


    public void deleteProjectDocs(String projectId) {
        try {

            if (projectId == null || projectId.trim().isEmpty()) {
                throw new IllegalArgumentException("ProjectId cannot be null or empty");
            }

            String filterExpression = "projectId == '" + projectId.replace("'", "''") + "'";
            vectorStore.delete(filterExpression);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete project documents: " + e.getMessage(), e);
        }
    }

    public void deleteProjectConversations(String conversationId) {
        try {

            String filterExpression = "conversationId == '" + conversationId.replace("'", "''") + "'";
            vectorStore.delete(filterExpression);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete conversations " + e.getMessage(), e);
        }
    }


}
