package com.docintel.docintel_backend.repository;

import com.docintel.docintel_backend.entity.FeedbackPattern;
import com.docintel.docintel_backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeedbackPatternRepository extends JpaRepository<FeedbackPattern, java.util.UUID> {
    Optional<FeedbackPattern> findByFieldLabelAndDocType(String fieldLabel, Document.DocType docType);
}