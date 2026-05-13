package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Default email transport while SMTP/{@code JavaMailSender} is not selected.
 * <ul>
 *   <li>Always logs send attempts (INFO/ERROR)</li>
 *   <li>When {@code app.mail.enabled=true}, writes a MIME {@code .eml} file to the outbox directory</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "transport", havingValue = "logging", matchIfMissing = true)
@RequiredArgsConstructor
public class LoggingEmailSender implements EmailSender {

    private final MailProperties mailProperties;

    @Override
    public void send(EmailMessage message) {
        log.info("EMAIL REQUEST → to={}, cc={}, subject={}, attachments={}",
                message.getTo(),
                message.getCc(),
                message.getSubject(),
                message.getAttachments().size());

        try {
            if (mailProperties.isEnabled()) {
                Path eml = writeEml(message);
                log.info("EMAIL SENT (outbox) → to={}, file={}", message.getTo(), eml.toAbsolutePath());
            } else {
                log.info("EMAIL SENT (log-only, app.mail.enabled=false) → to={}, subject={}",
                        message.getTo(), message.getSubject());
            }
        } catch (Exception ex) {
            log.error("EMAIL FAILED → to={}, subject={}, reason={}",
                    message.getTo(), message.getSubject(), ex.getMessage(), ex);
            throw new EmailDeliveryException("Failed to deliver email to " + message.getTo(), ex);
        }
    }

    private Path writeEml(EmailMessage message) throws IOException {
        Path dir = Path.of(mailProperties.getOutboxDirectory());
        Files.createDirectories(dir);
        String safeSubject = message.getSubject().replaceAll("[^a-zA-Z0-9-_]", "_");
        Path file = dir.resolve(Instant.now().toEpochMilli() + "_" + safeSubject + "_" + UUID.randomUUID() + ".eml");

        String boundary = "----=_Part_" + UUID.randomUUID();
        StringBuilder mime = new StringBuilder();
        mime.append("From: ").append(mailProperties.getFromName())
                .append(" <").append(mailProperties.getFrom()).append(">\r\n");
        mime.append("To: ").append(message.getTo()).append("\r\n");
        if (StringUtils.hasText(message.getCc())) {
            mime.append("Cc: ").append(message.getCc()).append("\r\n");
        }
        mime.append("Subject: ").append(message.getSubject()).append("\r\n");
        mime.append("MIME-Version: 1.0\r\n");
        mime.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n\r\n");

        mime.append("--").append(boundary).append("\r\n");
        mime.append("Content-Type: text/html; charset=UTF-8\r\n");
        mime.append("Content-Transfer-Encoding: 7bit\r\n\r\n");
        mime.append(message.getHtmlBody()).append("\r\n\r\n");

        for (EmailMessage.EmailAttachment attachment : message.getAttachments()) {
            mime.append("--").append(boundary).append("\r\n");
            mime.append("Content-Type: ").append(attachment.contentType())
                    .append("; name=\"").append(attachment.filename()).append("\"\r\n");
            mime.append("Content-Transfer-Encoding: base64\r\n");
            mime.append("Content-Disposition: attachment; filename=\"")
                    .append(attachment.filename()).append("\"\r\n\r\n");
            mime.append(Base64.getMimeEncoder().encodeToString(attachment.content())).append("\r\n\r\n");
        }
        mime.append("--").append(boundary).append("--\r\n");

        try (OutputStream out = Files.newOutputStream(file)) {
            out.write(mime.toString().getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }
}
