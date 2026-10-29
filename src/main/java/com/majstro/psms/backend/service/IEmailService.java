package com.majstro.psms.backend.service;

public interface IEmailService {
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String htmlBody);

    default void sendHtmlEmailWithReplyTo(String to, String replyToEmail, String replyToName, String subject, String htmlBody) {
        sendHtmlEmail(to, subject, htmlBody);
    }
}
