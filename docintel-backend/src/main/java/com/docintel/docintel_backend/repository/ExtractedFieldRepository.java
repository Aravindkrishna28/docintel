package com.docintel.docintel_backend.repository;

import com.docintel.docintel_backend.entity.ExtractedField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtractedFieldRepository extends JpaRepository<ExtractedField, UUID> {
    List<ExtractedField> findByDocumentId(UUID documentId);
}