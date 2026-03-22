package com.majstro.psms.backend.rag.dataModel;

public class MetaData {

    public String type;
    public String subType;
    public String userId;
    public String projectId;
    public String conversationId;


    public MetaData() {
        this.subType = " ";
        this.conversationId = " ";
    }
}
