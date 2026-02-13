package com.majstro.psms.backend.rag.dataModel;

import java.util.Map;

public class RAGDocument {

    private String id;                 // unique id for each source item
    private String content;            // text to embed
    private Map<String, Object> metadata; // source type, timestamps, sender, etc.

    public RAGDocument(String id, String content, Map<String,Object> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }

    // getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public Map<String,Object> getMetadata() { return metadata; }
}
