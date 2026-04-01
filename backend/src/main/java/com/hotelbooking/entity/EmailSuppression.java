package com.hotelbooking.entity;

import com.hotelbooking.database.EmailSuppressionReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "email_suppressions",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_suppressions_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class EmailSuppression extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 40)
    private EmailSuppressionReason reason;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
