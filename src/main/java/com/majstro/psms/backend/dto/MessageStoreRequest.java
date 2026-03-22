package com.majstro.psms.backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
@Setter
public class MessageStoreRequest {

    String conversationId;
    String message;
    String role;
    String userId;

}
