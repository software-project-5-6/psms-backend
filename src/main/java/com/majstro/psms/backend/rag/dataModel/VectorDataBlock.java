package com.majstro.psms.backend.rag.dataModel;

import java.util.HashMap;
import java.util.Map;

public class VectorDataBlock {

    private String id;                 // unique id for each source item
    private String content;            // text to embed
    private MetaData metadata; // source type, timestamps, sender, etc.

    public VectorDataBlock(String id, String content, MetaData metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }

    // getters
    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("type", metadata.type);
        metadataMap.put("subType", metadata.subType);
        metadataMap.put("userId", metadata.userId);
        metadataMap.put("projectId", metadata.projectId);
        metadataMap.put("conversationId", metadata.conversationId);

        return metadataMap;
    }
}
