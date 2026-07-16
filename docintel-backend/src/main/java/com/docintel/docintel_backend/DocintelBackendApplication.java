package com.docintel.docintel_backend;

import com.docintel.docintel_backend.dto.DocumentDetailResponse;
import com.docintel.docintel_backend.dto.FieldDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootApplication
public class    DocintelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocintelBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner testDto(ObjectMapper mapper) {
        return args -> {
            FieldDto field = new FieldDto("f1", "Invoice Number", "INV-2024-0157", 0.94, "HIGH");
            DocumentDetailResponse response = new DocumentDetailResponse(
                    "doc-123", "invoice.pdf", "PDF", "INVOICE", "DONE",
                    "This is a test summary.", null, List.of(field)
            );
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        };
    }
}