package com.docintel.docintel_backend.service;

import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.ExtractedField;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummarizationService {

    public String summarize(Document.DocType docType, List<ExtractedField> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("This document was classified as a")
                .append(docType == Document.DocType.INVOICE ? "n " : " ")
                .append(docType.name().toLowerCase())
                .append(". ");

        for (ExtractedField f : fields) {
            if (!f.getValue().isBlank()) {
                sb.append(f.getLabel()).append(": ").append(f.getValue()).append(". ");
            }
        }

        boolean hasLowConfidence = fields.stream()
                .anyMatch(f -> f.getLevel() != ExtractedField.ConfidenceLevel.HIGH);
        if (hasLowConfidence) {
            sb.append("Some fields were extracted with lower confidence and should be reviewed.");
        }

        return sb.toString().trim();
    }
}