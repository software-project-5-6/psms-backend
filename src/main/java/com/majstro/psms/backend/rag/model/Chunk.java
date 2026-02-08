package com.majstro.psms.backend.rag.model;

import java.util.Map;

public class Chunk {
    private String id;
    private String documentId;
    private String text;
    private Map<String, Object> metadata;
}
