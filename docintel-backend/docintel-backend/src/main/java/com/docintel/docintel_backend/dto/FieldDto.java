package com.docintel.docintel_backend.dto;

public class FieldDto {

    private String id;
    private String label;
    private String value;
    private Double confidence;
    private String level;

    public FieldDto() {}

    public FieldDto(String id, String label, String value, Double confidence, String level) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.confidence = confidence;
        this.level = level;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}