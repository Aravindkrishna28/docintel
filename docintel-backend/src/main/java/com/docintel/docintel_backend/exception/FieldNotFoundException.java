package com.docintel.docintel_backend.exception;

import java.util.UUID;

public class FieldNotFoundException extends RuntimeException {
    public FieldNotFoundException(UUID id) {
        super("No extracted field found with id: " + id);
    }
}