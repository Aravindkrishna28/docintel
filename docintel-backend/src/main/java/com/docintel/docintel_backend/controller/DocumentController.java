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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final ExtractedFieldRepository fieldRepository;

    public DocumentController(DocumentRepository documentRepository,
                              ExtractedFieldRepository fieldRepository) {
        this.documentRepository = documentRepository;
        this.fieldRepository = fieldRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setFilePath("/uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename());
        doc.setFileType(resolveFileType(file.getOriginalFilename()));
        documentRepository.save(doc);

        DocumentUploadResponse response = new DocumentUploadResponse(
                doc.getId().toString(),
                doc.getFilename(),
                doc.getStatus().name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<DocumentDetailResponse> processDocument(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        // --- MOCKED AI PIPELINE — Phase 4 replaces this block with real OCR/NLP ---
        doc.setDocType(Document.DocType.INVOICE);
        doc.setStatus(Document.DocumentStatus.DONE);
        doc.setSummary("This is a commercial invoice. Vendor and total amount fields " +
                "were extracted with lower confidence and should be reviewed.");
        documentRepository.save(doc);

        saveMockField(doc, "Invoice Number", "INV-2024-0157", 0.94, ExtractedField.ConfidenceLevel.HIGH);
        saveMockField(doc, "Date", "12/03/2024", 0.89, ExtractedField.ConfidenceLevel.HIGH);
        saveMockField(doc, "Vendor Name", "Nexora Supplies Pvt Ltd", 0.71, ExtractedField.ConfidenceLevel.MEDIUM);
        saveMockField(doc, "Total Amount", "₹ 42,850.00", 0.48, ExtractedField.ConfidenceLevel.LOW);
        // --- END MOCKED BLOCK ---

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

    private void saveMockField(Document doc, String label, String value,
                               double confidence, ExtractedField.ConfidenceLevel level) {
        ExtractedField field = new ExtractedField();
        field.setDocument(doc);
        field.setLabel(label);
        field.setValue(value);
        field.setConfidence(confidence);
        field.setLevel(level);
        fieldRepository.save(field);
    }

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