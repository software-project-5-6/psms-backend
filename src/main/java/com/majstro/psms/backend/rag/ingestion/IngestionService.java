package com.majstro.psms.backend.rag.ingestion;


import com.majstro.psms.backend.rag.model.RAGDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IngestionService {

    @Autowired
    VectorStore vectorStore;

    @Autowired
    TokenTextSplitter splitter;


    public void indexRagDocument(RAGDocument ragDoc) {

        Document doc = new Document(ragDoc.getContent(), ragDoc.getMetadata());
        List<Document> chunks = splitter.split(doc);
        vectorStore.add(chunks);        // embeddings + storage handled automatically
    }

}
