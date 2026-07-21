package com.docintel.docintel_backend.service;

import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.FeedbackPattern;
import com.docintel.docintel_backend.repository.FeedbackPatternRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private static final int ERROR_THRESHOLD = 3;

    private final FeedbackPatternRepository feedbackPatternRepository;

    public FeedbackService(FeedbackPatternRepository feedbackPatternRepository) {
        this.feedbackPatternRepository = feedbackPatternRepository;
    }

    public void recordCorrection(String fieldLabel, Document.DocType docType) {
        FeedbackPattern pattern = feedbackPatternRepository
                .findByFieldLabelAndDocType(fieldLabel, docType)
                .orElseGet(() -> {
                    FeedbackPattern p = new FeedbackPattern();
                    p.setFieldLabel(fieldLabel);
                    p.setDocType(docType);
                    p.setErrorCount(0);
                    return p;
                });

        pattern.setErrorCount(pattern.getErrorCount() + 1);
        pattern.setLastSeenAt(LocalDateTime.now());

        if (pattern.getErrorCount() >= ERROR_THRESHOLD) {
            pattern.setSuggestedConfidenceCap(0.60);
        }

        feedbackPatternRepository.save(pattern);
    }

    public Double getConfidenceCap(String fieldLabel, Document.DocType docType) {
        return feedbackPatternRepository
                .findByFieldLabelAndDocType(fieldLabel, docType)
                .map(FeedbackPattern::getSuggestedConfidenceCap)
                .orElse(null);
    }
}