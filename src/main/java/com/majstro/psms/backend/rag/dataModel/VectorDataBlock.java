package com.majstro.psms.backend.rag.dataModel;

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

    public MetaData getMetadata() {
        return metadata;
    }
}
