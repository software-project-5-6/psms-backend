package com.majstro.psms.backend.rag.model;

import java.util.Map;

public class VectorDocument {
    private String id;
    private String documentId;
    private String text;
    private float[] vector;
    private Map<String, Object> metadata;
}
