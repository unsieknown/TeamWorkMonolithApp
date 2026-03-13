package com.mordiniaa.backend.exceptions;

public class TaskCategoryNotFoundException extends RuntimeException {
    public TaskCategoryNotFoundException(String message) {
        super(message);
    }
}
