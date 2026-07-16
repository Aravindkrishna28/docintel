package com.docintel.docintel_backend.service;

import com.docintel.docintel_backend.entity.Document;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

    public Document.DocType classify(String rawText) {
        String lower = rawText.toLowerCase();
        if (lower.contains("invoice")) {
            return Document.DocType.INVOICE;
        }
        if (lower.contains("resume") || lower.contains("curriculum vitae")
                || (lower.contains("experience") && lower.contains("education"))) {
            return Document.DocType.RESUME;
        }
        return Document.DocType.UNKNOWN;
    }
}