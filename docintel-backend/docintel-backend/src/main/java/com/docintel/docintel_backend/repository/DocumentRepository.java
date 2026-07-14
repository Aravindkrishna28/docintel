package com.docintel.docintel_backend.repository;

import com.docintel.docintel_backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {}