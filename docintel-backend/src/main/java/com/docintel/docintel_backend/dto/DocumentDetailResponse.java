package com.docintel.docintel_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailResponse {
    private String documentId;
    private String filename;
    private String fileType;
    private String docType;
    private String status;
    private String summary;
    private String failureReason;
    private List<FieldDto> fields;
}