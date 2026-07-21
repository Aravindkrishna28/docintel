package com.docintel.docintel_backend.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String filename) {
        super("Unsupported file type for: " + filename + ". Only PDF, JPG, and PNG are allowed.");
    }
}