package com.majstro.psms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OutputChatMessage {
    public String id;
    public String role;
    public String content;
}
