package com.docintel.docintel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "extracted_fields")
@Getter
@Setter
@NoArgsConstructor
public class ExtractedField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private com.docintel.docintel_backend.entity.Document document;

    @Column(nullable = false)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(nullable = false)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfidenceLevel level;

    @OneToMany(mappedBy = "field", cascade = CascadeType.PERSIST)
    private List<com.docintel.docintel_backend.entity.Correction> corrections = new ArrayList<>();

    public enum ConfidenceLevel { HIGH, MEDIUM, LOW }
}