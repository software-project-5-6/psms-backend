package com.majstro.psms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectConversations {
    public String projectId;
    public List<OutputConversation> conversations;
}
