package com.docintel.docintel_backend.service;

import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.ExtractedField;
import com.docintel.docintel_backend.exception.UnsupportedFileTypeException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExtractionService {

    private static final Pattern INVOICE_NUMBER =
            Pattern.compile("(?i)invoice\\s*(?:number|no\\.?|#)\\s*[:\\-]?\\s*([A-Za-z0-9\\-/]+)");

    private static final Pattern DATE =
            Pattern.compile("\\b(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4})\\b");

    private static final Pattern TOTAL_AMOUNT =
            Pattern.compile("(?i)total\\s*amount\\s*[:\\-]?\\s*(?:rs\\.?|inr|₹)?\\s*([\\d,]+\\.\\d{2})");
    public List<ExtractedField> extractFields(String rawText, Document.DocType docType) {
        List<ExtractedField> fields = new ArrayList<>();

        fields.add(buildField("Invoice Number", find(INVOICE_NUMBER, rawText), docType));
        fields.add(buildField("Date", find(DATE, rawText), docType));
        fields.add(buildField("Vendor Name", guessVendorName(rawText), docType));
        fields.add(buildField("Total Amount", find(TOTAL_AMOUNT, rawText), docType));

        return fields;
    }

    private String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
    private final FeedbackService feedbackService;

    public ExtractionService(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }
    private String guessVendorName(String text) {
        // Heuristic: the vendor name is usually the first non-blank line
        // on a letterhead-style invoice. This is a weaker signal than
        // the regex-based fields above, so it gets MEDIUM confidence below.
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.length() > 3 && !trimmed.matches(".*\\d{3,}.*")) {
                return trimmed;
            }
        }
        return null;
    }
    private Document.FileType resolveFileType(String filename) {
        if (filename == null) {
            throw new UnsupportedFileTypeException("unknown");
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return Document.FileType.PDF;
        if (lower.endsWith(".png")) return Document.FileType.PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return Document.FileType.JPG;
        throw new UnsupportedFileTypeException(filename);
    }




    private ExtractedField buildField(String label, String value, Document.DocType docType) {
        ExtractedField field = new ExtractedField();
        field.setLabel(label);

        double confidence;
        ExtractedField.ConfidenceLevel level;

        if (value == null || value.isBlank()) {
            field.setValue("");
            confidence = 0.30;
            level = ExtractedField.ConfidenceLevel.LOW;
        } else if (label.equals("Vendor Name")) {
            field.setValue(value);
            confidence = 0.65;
            level = ExtractedField.ConfidenceLevel.MEDIUM;
        } else {
            field.setValue(value);
            confidence = 0.90;
            level = ExtractedField.ConfidenceLevel.HIGH;
        }

        Double cap = feedbackService.getConfidenceCap(label, docType);
        if (cap != null && confidence > cap) {
            confidence = cap;
            level = ExtractedField.ConfidenceLevel.MEDIUM;
        }

        field.setConfidence(confidence);
        field.setLevel(level);
        return field;
    }
}