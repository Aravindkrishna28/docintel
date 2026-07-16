package com.docintel.docintel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "feedback_patterns",
        uniqueConstraints = @UniqueConstraint(columnNames = {"field_label", "doc_type"})
)
@Getter
@Setter
@NoArgsConstructor
public class FeedbackPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "field_label", nullable = false)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)

    private com.docintel.docintel_backend.entity.Document.DocType docType;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "suggested_confidence_cap")
    private Double suggestedConfidenceCap;
}