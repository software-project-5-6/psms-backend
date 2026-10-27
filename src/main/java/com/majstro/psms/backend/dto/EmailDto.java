package com.majstro.psms.backend.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class EmailDto {
    public String to;
    public String from;
    public String subject;
    public String body;

    public EmailDto() {
    }

    public EmailDto(String to, String subject, String body, String from) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.from = from;
    }
}
