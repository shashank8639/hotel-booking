package com.hotelbooking.entity;

import com.hotelbooking.database.EmailOutboxStatus;
import com.hotelbooking.database.EmailType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class EmailOutbox extends BaseEntity {

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    @Column(name = "cc_address", length = 255)
    private String ccAddress;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 40)
    private EmailType emailType;

    @Column(name = "locale", nullable = false, length = 10)
    @Builder.Default
    private String locale = "en";

    @Lob
    @Column(name = "body_html", columnDefinition = "MEDIUMTEXT")
    private String bodyHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmailOutboxStatus status = EmailOutboxStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
