package com.docintel.docintel_backend;

import com.docintel.docintel_backend.entity.Document;
import com.docintel.docintel_backend.entity.ExtractedField;
import com.docintel.docintel_backend.repository.DocumentRepository;
import com.docintel.docintel_backend.repository.ExtractedFieldRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DocintelBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocintelBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner testSchema(DocumentRepository docRepo, ExtractedFieldRepository fieldRepo) {
		return args -> {
			Document doc = new Document();
			doc.setFilename("test-invoice.pdf");
			doc.setFilePath("/uploads/test-invoice.pdf");
			doc.setFileType(Document.FileType.PDF);
			docRepo.save(doc);

			ExtractedField field = new ExtractedField();
			field.setDocument(doc);
			field.setLabel("Invoice Number");
			field.setValue("INV-TEST-001");
			field.setConfidence(0.92);
			field.setLevel(ExtractedField.ConfidenceLevel.HIGH);
			fieldRepo.save(field);

			System.out.println("Saved document: " + docRepo.findById(doc.getId()).orElseThrow().getFilename());
		};
	}
}