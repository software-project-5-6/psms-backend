package com.majstro.psms.backend.dto;


import lombok.*;

@Getter
@Setter
public class AskRequest {

    String projectId;
    String conversationId;
    String question;
//    Integer topK,
//    Double temperature

}
