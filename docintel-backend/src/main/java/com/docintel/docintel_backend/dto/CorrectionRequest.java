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
public class CorrectionRequest {
    private List<CorrectionItem> corrections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectionItem {
        private String fieldId;
        private String correctedValue;
    }
}