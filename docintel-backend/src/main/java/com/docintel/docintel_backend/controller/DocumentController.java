package com.docintel.docintel_backend.controller;

import com.docintel.docintel_backend.dto.CorrectionRequest;
import com.docintel.docintel_backend.dto.DocumentDetailResponse;
import com.docintel.docintel_backend.dto.DocumentUploadResponse;
import com.docintel.docintel_backend.dto.FieldDto;
import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.ExtractedField;
import com.docintel.docintel_backend.repository.DocumentRepository;
import com.docintel.docintel_backend.repository.ExtractedFieldRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.docintel.docintel_backend.service.OcrService;
import com.docintel.docintel_backend.service.ClassificationService;
import com.docintel.docintel_backend.service.ExtractionService;
import com.docintel.docintel_backend.service.SummarizationService;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final ExtractedFieldRepository fieldRepository;
    private final com.docintel.docintel_backend.service.OcrService ocrService;
    private final com.docintel.docintel_backend.service.ClassificationService classificationService;
    private final com.docintel.docintel_backend.service.ExtractionService extractionService;
    private final com.docintel.docintel_backend.service.SummarizationService summarizationService;
    private final String uploadDir;

    public DocumentController(DocumentRepository documentRepository,
                              ExtractedFieldRepository fieldRepository,
                              com.docintel.docintel_backend.service.OcrService ocrService,
                              com.docintel.docintel_backend.service.ClassificationService classificationService,
                              com.docintel.docintel_backend.service.ExtractionService extractionService,
                              com.docintel.docintel_backend.service.SummarizationService summarizationService,
                              @Value("${app.upload-dir}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.fieldRepository = fieldRepository;
        this.ocrService = ocrService;
        this.classificationService = classificationService;
        this.extractionService = extractionService;
        this.summarizationService = summarizationService;
        this.uploadDir = uploadDir;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) throws java.io.IOException {

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setFileType(resolveFileType(file.getOriginalFilename()));
        doc.setFilePath("pending");
        documentRepository.save(doc);

        String storedFilename = doc.getId() + "-" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir, storedFilename);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, file.getBytes());

        doc.setFilePath(targetPath.toString());
        documentRepository.save(doc);

        DocumentUploadResponse response = new DocumentUploadResponse(
                doc.getId().toString(), doc.getFilename(), doc.getStatus().name());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/process")
    public ResponseEntity<DocumentDetailResponse> processDocument(@PathVariable UUID id) throws Exception {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        File file = new File(doc.getFilePath());
        String rawText = ocrService.extractText(file);

        Document.DocType docType = classificationService.classify(rawText);
        doc.setDocType(docType);

        List<ExtractedField> extractedFields = extractionService.extractFields(rawText, docType);
        for (ExtractedField field : extractedFields) {
            field.setDocument(doc);
            fieldRepository.save(field);
        }

        String summary = summarizationService.summarize(docType, extractedFields);
        doc.setSummary(summary);
        doc.setStatus(Document.DocumentStatus.DONE);
        documentRepository.save(doc);

        return ResponseEntity.ok(toDetailResponse(doc));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> getDocument(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        return ResponseEntity.ok(toDetailResponse(doc));
    }

    @PutMapping("/{id}/corrections")
    public ResponseEntity<DocumentDetailResponse> submitCorrections(
            @PathVariable UUID id,
            @RequestBody CorrectionRequest request) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        for (CorrectionRequest.CorrectionItem item : request.getCorrections()) {
            UUID fieldId = UUID.fromString(item.getFieldId());
            ExtractedField field = fieldRepository.findById(fieldId)
                    .orElseThrow(() -> new RuntimeException("Field not found: " + fieldId));

            field.setValue(item.getCorrectedValue());
            field.setLevel(ExtractedField.ConfidenceLevel.HIGH);
            field.setConfidence(0.97);
            fieldRepository.save(field);

            // NOTE: Correction history + FeedbackPattern logic is Phase 6 — not built yet
        }

        return ResponseEntity.ok(toDetailResponse(doc));
    }

    // ---------- Private helpers ----------


    private DocumentDetailResponse toDetailResponse(Document doc) {
        List<FieldDto> fieldDtos = fieldRepository.findByDocumentId(doc.getId()).stream()
                .map(f -> new FieldDto(
                        f.getId().toString(),
                        f.getLabel(),
                        f.getValue(),
                        f.getConfidence(),
                        f.getLevel().name()
                ))
                .collect(Collectors.toList());

        return new DocumentDetailResponse(
                doc.getId().toString(),
                doc.getFilename(),
                doc.getFileType() != null ? doc.getFileType().name() : null,
                doc.getDocType() != null ? doc.getDocType().name() : null,
                doc.getStatus().name(),
                doc.getSummary(),
                doc.getFailureReason(),
                fieldDtos
        );
    }

    private Document.FileType resolveFileType(String filename) {
        if (filename == null) return Document.FileType.PDF;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return Document.FileType.PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return Document.FileType.JPG;
        return Document.FileType.PDF;
    }
}