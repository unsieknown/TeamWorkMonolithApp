package com.mordiniaa.backend.exceptions;

public class StorageQuotaExceededException extends RuntimeException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
