package com.docintel.docintel_backend.service;

import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.ExtractedField;
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

        fields.add(buildField("Invoice Number", find(INVOICE_NUMBER, rawText)));
        fields.add(buildField("Date", find(DATE, rawText)));
        fields.add(buildField("Vendor Name", guessVendorName(rawText)));
        fields.add(buildField("Total Amount", find(TOTAL_AMOUNT, rawText)));

        return fields;
    }

    private String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
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

    private ExtractedField buildField(String label, String value) {
        ExtractedField field = new ExtractedField();
        field.setLabel(label);

        if (value == null || value.isBlank()) {
            field.setValue("");
            field.setConfidence(0.30);
            field.setLevel(ExtractedField.ConfidenceLevel.LOW);
        } else if (label.equals("Vendor Name")) {
            // heuristic-based field: always capped at MEDIUM, never HIGH
            field.setValue(value);
            field.setConfidence(0.65);
            field.setLevel(ExtractedField.ConfidenceLevel.MEDIUM);
        } else {
            // regex-matched field: high confidence
            field.setValue(value);
            field.setConfidence(0.90);
            field.setLevel(ExtractedField.ConfidenceLevel.HIGH);
        }
        return field;
    }
}