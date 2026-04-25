package com.hotelbooking.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory representation of an outbound email (HTML body + optional attachments).
 */
public class EmailMessage {

    private String to;
    private String cc;
    private String subject;
    private String htmlBody;
    private final List<EmailAttachment> attachments = new ArrayList<>();

    public String getTo() {
        return to;
    }

    public EmailMessage setTo(String to) {
        this.to = to;
        return this;
    }

    public String getCc() {
        return cc;
    }

    public EmailMessage setCc(String cc) {
        this.cc = cc;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public EmailMessage setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public EmailMessage setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
        return this;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public EmailMessage addAttachment(EmailAttachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public record EmailAttachment(String filename, String contentType, byte[] content) {
    }
}
